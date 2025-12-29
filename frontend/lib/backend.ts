import "server-only";
import { cookies } from "next/headers";

const baseUrl = process.env.BACKEND_BASE_URL ?? "http://localhost:8080";
const tokenCookie = process.env.JWT_COOKIE_NAME ?? "ebank_token";
const userCookie = process.env.USER_COOKIE_NAME ?? "ebank_user";

type HttpMethod = "GET" | "POST";

export class BackendError extends Error {
  constructor(
    message: string,
    public status: number,
    public bodyText?: string
  ) {
    super(message);
  }
}

export async function getUsername(): Promise<string> {
  const c = await cookies();
  return c.get(userCookie)?.value ?? "";
}

async function getToken(): Promise<string | undefined> {
  const c = await cookies();
  return c.get(tokenCookie)?.value;
}

// ✅ set/clear も async に統一（15.4 の dynamic API 対応）
export async function setSession(token: string, username: string) {
  const c = await cookies();
  c.set(tokenCookie, token, {
    httpOnly: true,
    sameSite: "lax",
    secure: process.env.NODE_ENV === "production",
    path: "/",
  });
  // username は表示用（不要なら消してOK）
  c.set(userCookie, username, {
    httpOnly: false,
    sameSite: "lax",
    secure: process.env.NODE_ENV === "production",
    path: "/",
  });
}

export async function clearSession() {
  const c = await cookies();
  c.set(tokenCookie, "", { httpOnly: true, path: "/", maxAge: 0 });
  c.set(userCookie, "", { httpOnly: false, path: "/", maxAge: 0 });
}

export async function backend<T>(
  path: string,
  opts: { method: HttpMethod; body?: unknown; auth?: boolean }
): Promise<T> {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
  };

  if (opts.auth) {
    const token = await getToken();
    if (token) headers["Authorization"] = `Bearer ${token}`;
  }

  const res = await fetch(`${baseUrl}${path}`, {
    method: opts.method,
    headers,
    body: opts.body !== undefined ? JSON.stringify(opts.body) : undefined,
    cache: "no-store",
  });

  if (!res.ok) {
    const text = await res.text().catch(() => "");
    throw new BackendError(`Backend ${res.status} ${path}`, res.status, text);
  }

  const ct = res.headers.get("content-type") ?? "";
  if (ct.includes("application/json")) return (await res.json()) as T;
  return (await res.text()) as unknown as T;
}
