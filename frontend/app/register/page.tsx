import Link from "next/link";
import { registerAction } from "@/app/actions/auth";

export default async function RegisterPage({
  searchParams,
}: {
  searchParams: Promise<{ error?: string }>;
}) {
  const sp = await searchParams;
  const error = sp.error ?? "";

  let message: string | null = null;
  if (error === "pw_mismatch") {
    message = "パスワードが一致しません。";
  } else if (error === "register_failed") {
    message = "ユーザ登録に失敗しました。既に使われているユーザ名の可能性があります。";
  }

  return (
    <main className="container" style={{ maxWidth: 520, paddingTop: 48 }}>
      <div className="card shadow-sm">
        <div className="card-body p-4">
          <h3 className="mb-3">eBank 新規登録</h3>

          {message ? <div className="alert alert-danger">{message}</div> : null}

          <form action={registerAction} className="d-grid gap-3">
            <div>
              <label className="form-label">ユーザー名</label>
              <input name="username" className="form-control" autoComplete="username" />
            </div>
            <div>
              <label className="form-label">パスワード</label>
              <input name="password" type="password" className="form-control" autoComplete="new-password" />
            </div>
            <div>
              <label className="form-label">パスワード（確認）</label>
              <input name="password_confirm" type="password" className="form-control" autoComplete="new-password" />
            </div>

            <button className="btn btn-primary" type="submit">
              登録
            </button>
          </form>

          <div className="mt-3">
            <Link href="/login">ログインはこちら</Link>
          </div>
        </div>
      </div>
    </main>
  );
}
