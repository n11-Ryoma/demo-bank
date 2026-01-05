import "server-only";
import { cookies } from "next/headers";

const baseUrl = process.env.BACKEND_BASE_URL ?? "http://localhost:8080";
const tokenCookie = process.env.JWT_COOKIE_NAME ?? "ebank_token";
const userCookie = process.env.USER_COOKIE_NAME ?? "ebank_user";

type HttpMethod = "GET" | "POST";

export class BackendError extends Error {
  constructor(message: string, public status: number, public bodyText?: string) {
    super(message);
  }
}

export async function getUsername(): Promise<string | null> {
  const c = await cookies();
  return c.get(userCookie)?.value ?? null;
}

function bearer(token: string) {
  return token.startsWith("Bearer ") ? token : `Bearer ${token}`;
}

export async function backend<T>(
  path: string,
  opts: { method: HttpMethod; body?: unknown; auth?: boolean }
): Promise<T> {
  const c = await cookies();

  const headers: Record<string, string> = {
    Accept: "application/json",
  };

  if (opts.body !== undefined) {
    headers["Content-Type"] = "application/json";
  }

  // auth が false 以外なら token を付ける
  if (opts.auth !== false) {
    const token = c.get(tokenCookie)?.value;
    if (token) headers["Authorization"] = bearer(token);
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
