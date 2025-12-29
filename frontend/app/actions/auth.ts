"use server";

import { redirect } from "next/navigation";
import { backend, clearSession, setSession } from "@/lib/backend";

export async function loginAction(formData: FormData) {
  const username = String(formData.get("username") ?? "");
  const password = String(formData.get("password") ?? "");

  const res = await backend<{ token?: string }>("/api/auth/login", {
    method: "POST",
    body: { username, password },
  });

  if (!res.token) throw new Error("No token returned");
  setSession(res.token, username);

  redirect("/dashboard");
}

export async function logoutAction() {
  clearSession();
  redirect("/login");
}
