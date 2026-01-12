"use server";

import { backend, BackendError } from "@/lib/backend";
import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { promises as fs } from "fs";
import path from "path";
import crypto from "crypto";

const DRAFT_COOKIE = "ebank_addr_draft";
const FLASH_COOKIE = "ebank_flash";
const TMP_DIR = path.join(process.cwd(), ".tmp", "address_proofs");

type CurrentAddress = {
  postalCode: string;
  prefecture: string;
  city: string;
  addressLine1: string;
  addressLine2?: string;
};

type Draft = {
  postalCode: string;
  prefecture: string;
  city: string;
  addressLine1: string;
  addressLine2: string;
  tmpId: string;          // サーバ側一時ファイル名（uuid.ext）
  originalName: string;   // 元ファイル名
};

async function setFlash(message: string) {
  const c = await cookies();
  c.set(FLASH_COOKIE, message, { path: "/", httpOnly: true, sameSite: "lax" });
}

async function getDraft(): Promise<Draft | null> {
  const c = await cookies();
  const raw = c.get(DRAFT_COOKIE)?.value;
  if (!raw) return null;
  try {
    return JSON.parse(raw) as Draft;
  } catch {
    return null;
  }
}

async function setDraft(d: Draft) {
  const c = await cookies();
  c.set(DRAFT_COOKIE, JSON.stringify(d), { path: "/", httpOnly: true, sameSite: "lax" });
}

async function clearDraft() {
  const c = await cookies();
  c.delete(DRAFT_COOKIE);
}

function extOf(name: string) {
  const ext = path.extname(name || "").replace(".", "").toLowerCase();
  return ext || "pdf";
}

export async function getCurrentAddress(): Promise<CurrentAddress | null> {
  // PHP 側が叩いてたのと同じエンドポイント想定 :contentReference[oaicite:4]{index=4}
  try {
    return await backend<CurrentAddress>("/api/address-change/current", { method: "GET" });
  } catch (e) {
    if (e instanceof BackendError && e.status === 404) return null;
    throw e;
  }
}

export async function startAddressChangeAction(fd: FormData) {
  const postal = String(fd.get("postal") ?? "").trim();
  const pref = String(fd.get("pref") ?? "").trim();
  const city = String(fd.get("city") ?? "").trim();
  const address1 = String(fd.get("address1") ?? "").trim();
  const address2 = String(fd.get("address2") ?? "").trim();

  const file = fd.get("proof_file");
  if (!postal || !pref || !city || !address1) {
    await setFlash("住所情報を正しく入力してください。");
    redirect("/address");
  }
  if (!(file instanceof File) || file.size === 0) {
    await setFlash("住所確認書類をアップロードしてください。");
    redirect("/address");
  }

  // 一時ファイル保存（confirm でプレビューするため）
  await fs.mkdir(TMP_DIR, { recursive: true });
  const ext = extOf(file.name);
  const tmpId = `${crypto.randomUUID()}.${ext}`;
  const tmpPath = path.join(TMP_DIR, tmpId);

  const buf = Buffer.from(await file.arrayBuffer());
  await fs.writeFile(tmpPath, buf);

  await setDraft({
    postalCode: postal,
    prefecture: pref,
    city,
    addressLine1: address1,
    addressLine2: address2,
    tmpId,
    originalName: file.name,
  });

  // ★ redirect は try/catch に入れない（入れると “コード: ?” になりがち）
  redirect("/address/confirm");
}

export async function commitAddressChangeAction() {
  const draft = await getDraft();
  if (!draft) {
    await setFlash("住所変更情報が見つかりません。最初からやり直してください。");
    redirect("/address");
  }

  const tmpPath = path.join(TMP_DIR, draft.tmpId);

  try {
    const fileBytes = await fs.readFile(tmpPath);
    const fileBase64 = fileBytes.toString("base64");

    // PHP と同じキーで送る :contentReference[oaicite:5]{index=5}
    const body = {
      postalCode: draft.postalCode,
      prefecture: draft.prefecture,
      city: draft.city,
      addressLine1: draft.addressLine1,
      addressLine2: draft.addressLine2,
      fileName: draft.originalName,
      fileBase64,
    };

    await backend("/api/address-change/commit", { method: "POST", body });

    // 成功：一時データ削除
    await fs.unlink(tmpPath).catch(() => {});
    await clearDraft();
    await setFlash("住所変更の申請を受け付けました。審査結果をお待ちください。");

  } catch (e) {
    // 失敗：ステータスを flash に出す（? を潰す）
    if (e instanceof BackendError) {
      await setFlash(
        e.status === 401
          ? "ログインの有効期限が切れたか、認証に失敗しました。再ログインしてください。（コード: 401）"
          : `住所変更の確定に失敗しました。（コード: ${e.status}）`
      );
      // デバッグしたいなら e.bodyText もログ出しすると良い
      console.error("commit failed:", e.status, e.bodyText);
    } else {
      await setFlash("住所変更の確定に失敗しました。（コード: ?）");
      console.error("commit failed (unknown):", e);
    }

    redirect("/address/confirm?error=1");
  }

  // ★成功時の redirect は catch されない位置で
  redirect("/address");
}
