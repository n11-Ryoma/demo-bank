import Link from "next/link";
import { loginAction } from "@/app/actions/auth";

export default function LoginPage() {
  return (
    <main className="container" style={{ maxWidth: 520, paddingTop: 48 }}>
      <div className="card shadow-sm">
        <div className="card-body p-4">
          <h3 className="mb-3">eBank ログイン</h3>

          <form action={loginAction} className="d-grid gap-3">
            <div>
              <label className="form-label">ユーザー名</label>
              <input name="username" className="form-control" />
            </div>
            <div>
              <label className="form-label">パスワード</label>
              <input name="password" type="password" className="form-control" />
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
