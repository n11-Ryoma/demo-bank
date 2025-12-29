import AppShell from "@/components/AppShell";
import { getTransactions } from "@/app/actions/accounts";

export default async function TransactionsPage() {
  const items = await getTransactions(50, 0);

  return (
    <AppShell active="transactions">
      <div className="card shadow-sm">
        <div className="card-header bg-white">
          <h5 className="mb-0">取引明細</h5>
        </div>
        <div className="card-body">
          <div className="table-responsive">
            <table className="table table-striped table-hover">
              <thead>
                <tr>
                  <th>種別</th>
                  <th className="text-end">金額</th>
                  <th className="text-end">残高</th>
                  <th>相手口座</th>
                  <th>メモ</th>
                  <th>日時</th>
                </tr>
              </thead>
              <tbody>
                {items.map((x, i) => (
                  <tr key={i}>
                    <td>{x.type ?? ""}</td>
                    <td className="text-end">{(x.amount ?? 0).toLocaleString()}</td>
                    <td className="text-end">{(x.balanceAfter ?? 0).toLocaleString()}</td>
                    <td>{x.relatedAccountNumber ?? ""}</td>
                    <td>{x.description ?? ""}</td>
                    <td>{x.createdAt ?? ""}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          {items.length === 0 ? <div className="text-muted">明細はまだありません。</div> : null}
        </div>
      </div>
    </AppShell>
  );
}
