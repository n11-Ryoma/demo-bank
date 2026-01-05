"use server";

import { redirect } from "next/navigation";
import { cookies } from "next/headers";
import { backend, BackendError } from "@/lib/backend";

const tokenCookie = process.env.JWT_COOKIE_NAME ?? "ebank_token";
const userCookie = process.env.USER_COOKIE_NAME ?? "ebank_user";

// ✅ 追加：flash cookie
const flashCookie = "ebank_flash";

async function setFlash(message: string) {
  const c = await cookies();
  c.set(flashCookie, message, {
    httpOnly: true,
    sameSite: "lax",
    secure: process.env.NODE_ENV === "production",
    path: "/",
    maxAge: 30, // 30秒くらいで十分
  });
}

export async function loginAction(formData: FormData) {
  const username = String(formData.get("username") ?? "");
  const password = String(formData.get("password") ?? "");

  const c = await cookies();

  let token: string | null = null;
  try {
    const data = await backend<{ token?: string }>("/api/auth/login", {
      method: "POST",
      body: { username, password },
      auth: false,
    });
    token = typeof data?.token === "string" ? data.token : null;
  } catch {
    token = null;
  }

  if (!token) {
    // ✅ cookieもflashも使わず、クエリで表現
    redirect("/login?error=1");
  }

  c.set("ebank_token", token, {
    httpOnly: true,
    sameSite: "lax",
    secure: false,
    path: "/",
  });

  redirect("/dashboard");
}

export async function logoutAction() {
  const c = await cookies();
  c.set(tokenCookie, "", { httpOnly: true, path: "/", maxAge: 0 });
  c.set(userCookie, "", { httpOnly: false, path: "/", maxAge: 0 });
  redirect("/login");
}

// ✅ 登録：cookieなし。成功/失敗は URL クエリで表示
export async function registerAction(formData: FormData) {
  const username = String(formData.get("username") ?? "");
  const password = String(formData.get("password") ?? "");
  const passwordConfirm = String(formData.get("password_confirm") ?? "");

  if (password !== passwordConfirm) {
    redirect("/register?error=pw_mismatch");
  }

  let ok = false;

  try {
    // 返り値は不要。204/空bodyでもOK
    await backend<unknown>("/api/auth/register", {
      method: "POST",
      body: { username, password },
      auth: false, // ← 念のため（registerは未ログイン前提）
    });
    ok = true;
  } catch (e: any) {
    console.error("[REGISTER] failed", { status: e?.status, body: e?.bodyText });
    ok = false;
  }

  // ✅ redirect は try/catch の外で実行（これが重要）
  if (ok) {
    redirect("/login?registered=1");
  } else {
    redirect("/register?error=register_failed");
  }
}


