"use client";

import { useState } from "react";
import { registerAction } from "@/app/actions/auth";

export default function RegisterForm() {
  const [submitting, setSubmitting] = useState(false);

  return (
    <form
      action={async (fd) => {
        if (submitting) return; // 二重送信ガード
        setSubmitting(true);
        await registerAction(fd);
      }}
      className="mt-2"
    >
      <div className="mb-3">
        <label className="form-label">ユーザ名</label>
        <input type="text" name="username" className="form-control" required autoComplete="username" />
      </div>

      <div className="mb-3">
        <label className="form-label">パスワード</label>
        <input type="password" name="password" className="form-control" required autoComplete="new-password" />
      </div>

      <div className="mb-3">
        <label className="form-label">パスワード（確認）</label>
        <input type="password" name="password_confirm" className="form-control" required autoComplete="new-password" />
      </div>

      <button type="submit" className="btn btn-primary w-100" disabled={submitting}>
        {submitting ? "登録中..." : "登録する"}
      </button>
    </form>
  );
}
