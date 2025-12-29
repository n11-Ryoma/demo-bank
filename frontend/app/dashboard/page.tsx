import Link from "next/link";
import AppShell from "@/components/AppShell";
import { getBalance, transferAction } from "@/app/actions/accounts";

export default async function DashboardPage({
  searchParams,
}: {
  searchParams: Promise<{ ok?: string; err?: string }>;
}) {
  const sp = await searchParams; // ✅ Next 15.4: 先に await

  let balance: number | null = null;
  let accountNumber: string | null = null;
  let error: string | null = null;

  try {
    const res = await getBalance();
    balance = res.balance ?? null;
    accountNumber = res.accountNumber ?? null;
  } catch {
    error = "残高取得に失敗しました。";
  }

  const flashOk = sp.ok ? "振込が完了しました。" : null;
  const flashErr = sp.err ? "振込に失敗しました。" : null;

  return (
    <AppShell active="dashboard">
      {flashOk ? <div className="alert alert-success">{flashOk}</div> : null}
      {flashErr ? <div className="alert alert-danger">{flashErr}</div> : null}
      {error ? <div className="alert alert-danger">{error}</div> : null}

      {/* 残高カード */}
      <div className="card shadow-sm mb-4">
        <div className="card-body">
          <h5 className="card-title mb-3">メイン口座</h5>

          <p className="text-muted mb-1">口座番号</p>
          <p className="fs-6 fw-semibold">{accountNumber ?? "---"}</p>

          <p className="text-muted mb-1">現在残高</p>
          <p className="fs-2 fw-bold text-primary">
            {balance !== null ? `${balance.toLocaleString()} 円` : "---"}
          </p>

          <div className="row mt-4">
            <div className="col-12">
              <Link href="/transactions" className="btn btn-primary w-100">
                取引明細を見る
              </Link>
            </div>
          </div>
        </div>
      </div>

      {/* 振込フォーム */}
      <div id="transfer" className="card shadow-sm">
        <div className="card-header bg-white">
          <h5 className="mb-0">振込</h5>
        </div>
        <div className="card-body">
          <form action={transferAction} className="row g-3">
            <div className="col-md-4">
              <label className="form-label">振込先口座番号</label>
              <input
                type="text"
                name="toAccountNumber"
                className="form-control"
                required
              />
            </div>

            <div className="col-md-4">
              <label className="form-label">金額</label>
              <div className="input-group">
                <input
                  type="number"
                  name="amount"
                  className="form-control"
                  min={1}
                  required
                />
                <span className="input-group-text">円</span>
              </div>
            </div>

            <div className="col-md-4">
              <label className="form-label">メモ（任意）</label>
              <input
                type="text"
                name="description"
                className="form-control"
                placeholder="家賃など"
              />
            </div>

            <div className="col-12">
              <button className="btn btn-primary w-100" type="submit">
                振込する
              </button>
            </div>
          </form>

          {/* 住所変更ボタン（PHPと同じ位置/ノリ） */}
          <div className="row mt-3">
            <div className="col-12">
              <Link href="/address" className="btn btn-outline-primary w-100">
                住所変更のお手続き
              </Link>
            </div>
          </div>
        </div>
      </div>
    </AppShell>
  );
}
