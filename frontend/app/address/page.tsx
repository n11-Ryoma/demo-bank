import AppShell from "@/components/AppShell";
import FlashToast from "@/components/FlashToast";
import { cookies } from "next/headers";
import { getCurrentAddress, startAddressChangeAction } from "@/app/actions/addresses";

async function readFlash() {
  const c = await cookies(); // ★ await 必須
  return c.get("ebank_flash")?.value ?? null;
}

function formatAddress(a: any) {
  if (!a) return "現在登録されている住所はありません。";
  const p = a.postalCode ?? "";
  const pref = a.prefecture ?? "";
  const city = a.city ?? "";
  const l1 = a.addressLine1 ?? "";
  const l2 = a.addressLine2 ?? "";
  return `〒${p} ${pref}${city} ${l1} ${l2}`.trim();
}

export default async function AddressPage() {
  const flash = await readFlash();
  const current = await getCurrentAddress().catch(() => null);

  return (
    <AppShell active="address">
      <FlashToast message={flash} variant={flash?.includes("受け付け") ? "success" : "danger"} />

      <h2 className="mb-3">住所変更</h2>

      <div className="row g-4">
        <div className="col-lg-7">
          <div className="card shadow-sm mb-3">
            <div className="card-header bg-white">現在のご住所</div>
            <div className="card-body">
              <p className="mb-0">{formatAddress(current)}</p>
            </div>
          </div>

          <div className="card shadow-sm">
            <div className="card-header bg-white">新しいご住所</div>
            <div className="card-body">
              <form action={startAddressChangeAction} className="vstack gap-3">
                <div>
                  <label className="form-label">郵便番号</label>
                  <input className="form-control" name="postal" placeholder="1000001" required />
                </div>
                <div>
                  <label className="form-label">都道府県</label>
                  <input className="form-control" name="pref" placeholder="東京都" required />
                </div>
                <div>
                  <label className="form-label">市区町村</label>
                  <input className="form-control" name="city" placeholder="千代田区" required />
                </div>
                <div>
                  <label className="form-label">番地・建物名</label>
                  <input className="form-control" name="address1" placeholder="丸の内1-1-1 〇〇ビル 10F" required />
                </div>
                <div>
                  <label className="form-label">部屋番号・その他（任意）</label>
                  <input className="form-control" name="address2" placeholder="1001号室など" />
                </div>
                <div>
                  <label className="form-label">住所確認書類</label>
                  <input className="form-control" type="file" name="proof_file" required />
                </div>

                <button className="btn btn-primary w-100" type="submit">
                  入力内容を確認する
                </button>
              </form>
            </div>
          </div>
        </div>

        <div className="col-lg-5">
          <div className="alert alert-info">
            アップロード後、確認画面でプレビューを確認してから確定します。
          </div>
        </div>
      </div>
    </AppShell>
  );
}
