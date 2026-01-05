import Link from "next/link";
import { loginAction } from "@/app/actions/auth";

export default async function LoginPage({
  searchParams,
}: {
  searchParams: Promise<{ error?: string; registered?: string }>;
}) {
  const sp = await searchParams;

  const showError = sp.error === "1";
  const showRegistered = sp.registered === "1";

  return (
    <main className="container" style={{ maxWidth: 520, paddingTop: 48 }}>
      <div className="card shadow-sm">
        <div className="card-body p-4">
          <h3 className="mb-3">eBank ログイン</h3>

          {showError ? (
            <div className="alert alert-danger">ログインに失敗しました</div>
          ) : showRegistered ? (
            <div className="alert alert-success">
              ユーザ登録が完了しました。ログインしてください。
            </div>
          ) : null}

          <form action={loginAction} className="d-grid gap-3">
            <div>
              <label className="form-label">ユーザー名</label>
              <input
                name="username"
                className="form-control"
                autoComplete="username"
              />
            </div>
            <div>
              <label className="form-label">パスワード</label>
              <input
                name="password"
                type="password"
                className="form-control"
                autoComplete="current-password"
              />
            </div>
            <button className="btn btn-primary" type="submit">
              ログイン
            </button>
          </form>

          <div className="mt-3">
            <Link href="/register">新規登録はこちら</Link>
          </div>
        </div>
      </div>
    </main>
  );
}
