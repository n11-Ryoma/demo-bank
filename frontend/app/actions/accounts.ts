"use server";

import { redirect } from "next/navigation";
import { backend } from "@/lib/backend";

export async function getBalance() {
  return backend<{ accountNumber?: string; balance?: number }>("/api/accounts/balance", {
    method: "GET",
    auth: true,
  });
}

export async function getTransactions(limit = 20, offset = 0) {
  const qs = new URLSearchParams({ limit: String(limit), offset: String(offset) });
  return backend<
    Array<{
      type?: "DEPOSIT" | "WITHDRAW" | "TRANSFER_IN" | "TRANSFER_OUT";
      amount?: number;
      balanceAfter?: number;
      relatedAccountNumber?: string;
      description?: string;
      createdAt?: string;
    }>
  >(`/api/accounts/transactions?${qs.toString()}`, { method: "GET", auth: true });
}

export async function transferAction(_: unknown, formData: FormData) {
  const toAccountNumber = String(formData.get("toAccountNumber") ?? "");
  const amount = Number(formData.get("amount") ?? 0);
  const description = String(formData.get("description") ?? "");

  await backend("/api/accounts/transfer", {
    method: "POST",
    auth: true,
    body: { toAccountNumber, amount, description },
  });

  // PHPの flash success 的なやつ（最低限）
  redirect("/dashboard?ok=1#transfer");
}
