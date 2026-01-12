// app/api/address/preview/route.ts
export const runtime = "nodejs";

import { cookies } from "next/headers";
import { NextResponse } from "next/server";
import path from "path";
import fs from "fs/promises";

const uploadRoot = process.env.ADDRESS_UPLOAD_ROOT ?? path.join(process.cwd(), "uploads");

function decode<T>(v: string): T | null {
  try {
    return JSON.parse(decodeURIComponent(v)) as T;
  } catch {
    return null;
  }
}

function safeJoin(base: string, rel: string) {
  const p = path.join(base, rel);
  const nBase = path.normalize(base + path.sep);
  const nP = path.normalize(p);
  if (!nP.startsWith(nBase)) throw new Error("Invalid path");
  return nP;
}

function contentTypeByExt(ext: string) {
  switch (ext) {
    case "pdf":
      return "application/pdf";
    case "png":
      return "image/png";
    case "jpg":
    case "jpeg":
      return "image/jpeg";
    case "gif":
      return "image/gif";
    case "zip":
      return "application/zip";
    default:
      return "application/octet-stream";
  }
}

export async function GET(req: Request) {
  const c = await cookies();

  // 認証：JWT cookie がなければ拒否（必要に応じてロール/ユーザチェック追加）
  if (!c.get("ebank_token")?.value) {
    return NextResponse.json({ error: "unauthorized" }, { status: 401 });
  }

  const url = new URL(req.url);
  const file = url.searchParams.get("file") ?? "";

  // cookie に入ってる draft の filePath と一致するものだけ許可（任意のパスを読ませない）
  const raw = c.get("ebank_addr_change")?.value;
  const draft = raw ? decode<{ filePath: string }>(raw) : null;
  if (!draft || !draft.filePath || file !== draft.filePath) {
    return NextResponse.json({ error: "not found" }, { status: 404 });
  }

  if (!file.startsWith("tmp/address/")) {
    return NextResponse.json({ error: "invalid" }, { status: 400 });
  }

  const fullPath = safeJoin(uploadRoot, file);

  let buf: Buffer;
  try {
    buf = await fs.readFile(fullPath);
  } catch {
    return NextResponse.json({ error: "not found" }, { status: 404 });
  }

  const ext = path.extname(fullPath).replace(".", "").toLowerCase();
  const ct = contentTypeByExt(ext);

  return new NextResponse(buf, {
    headers: {
      "Content-Type": ct,
      "Content-Disposition": `inline; filename="${path.basename(fullPath)}"`,
    },
  });
}
