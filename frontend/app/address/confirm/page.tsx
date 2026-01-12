import AppShell from "@/components/AppShell";
import FlashToast from "@/components/FlashToast";
import { cookies } from "next/headers";
import Link from "next/link";
import { commitAddressChangeAction } from "@/app/actions/addresses";
import { redirect } from "next/navigation";

async function readFlash() {
  const c = await cookies();
  return c.get("ebank_flash")?.value ?? null;
}

async function readDraft() {
  const c = await cookies();
  const raw = c.get("ebank_addr_draft")?.value;
  if (!raw) return null;
  try {
    return JSON.parse(raw) as {
      postalCode: string;
      prefecture: string;
      city: string;
      addressLine1: string;
      addressLine2: string;
      tmpId: string;
      originalName: string;
    };
  } catch {
    return null;
  }
}

export default async function AddressConfirmPage() {
  const flash = await readFlash();
  const draft = await readDraft();

  if (!draft) redirect("/address");

  const ext = (draft.originalName.split(".").pop() || "").toLowerCase();
  const previewUrl = `/api/tmp/address/${encodeURIComponent(draft.tmpId)}`;

  return (
    <AppShell active="address">
      <FlashToast message={flash} variant="danger" />

      <h2 className="mb-3">住所変更内容の確認</h2>

      <div className="row g-4">
        <div className="col-lg-6">
          <div className="card shadow-sm mb-3">
            <div className="card-header bg-white">新しいご住所</div>
            <div className="card-body">
              <dl className="row mb-0">
                <dt className="col-sm-4">郵便番号</dt>
                <dd className="col-sm-8">{draft.postalCode}</dd>
                <dt className="col-sm-4">都道府県</dt>
                <dd className="col-sm-8">{draft.prefecture}</dd>
                <dt className="col-sm-4">市区町村</dt>
                <dd className="col-sm-8">{draft.city}</dd>
                <dt className="col-sm-4">番地・建物名</dt>
                <dd className="col-sm-8">{draft.addressLine1}</dd>
                <dt className="col-sm-4">部屋番号等</dt>
                <dd className="col-sm-8">{draft.addressLine2 || "（なし）"}</dd>
              </dl>
            </div>
          </div>

          <form action={commitAddressChangeAction} className="vstack gap-2">
            <button className="btn btn-primary w-100" type="submit">
              この内容で住所変更を確定する
            </button>
            <Link className="btn btn-secondary w-100" href="/address">
              修正する（入力画面に戻る）
            </Link>
          </form>
        </div>

        <div className="col-lg-6">
          <div className="card shadow-sm">
            <div className="card-header bg-white">住所確認書類のプレビュー</div>
            <div className="card-body">
              <p className="small mb-2">
                アップロードされたファイル：<code>{draft.originalName}</code>
              </p>

              {ext === "pdf" ? (
                <iframe src={previewUrl} className="w-100" style={{ height: 420 }} />
              ) : (
                // 画像系
                <img src={previewUrl} className="img-fluid border rounded" alt="proof preview" />
              )}
            </div>
          </div>
        </div>
      </div>
    </AppShell>
  );
}
