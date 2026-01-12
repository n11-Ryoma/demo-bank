"use client";

import { useState } from "react";
import { startAddressChangeAction } from "@/app/actions/addresses";

export default function AddressChangeForm() {
  const [submitting, setSubmitting] = useState(false);

  return (
    <form
      action={async (fd) => {
        if (submitting) return;
        setSubmitting(true);
        await startAddressChangeAction(fd);
      }}
      className="mt-2"
      encType="multipart/form-data"
    >
      <div className="mb-3">
        <label className="form-label">郵便番号</label>
        <input type="text" name="postal" className="form-control" placeholder="1000001" required />
      </div>

      <div className="mb-3">
        <label className="form-label">都道府県</label>
        <input type="text" name="pref" className="form-control" placeholder="東京都" required />
      </div>

      <div className="mb-3">
        <label className="form-label">市区町村</label>
        <input type="text" name="city" className="form-control" placeholder="千代田区" required />
      </div>

      <div className="mb-3">
        <label className="form-label">番地・建物名</label>
        <input
          type="text"
          name="address1"
          className="form-control"
          placeholder="丸の内1-1-1 〇〇ビル 10F"
          required
        />
      </div>

      <div className="mb-3">
        <label className="form-label">部屋番号・その他（任意）</label>
        <input type="text" name="address2" className="form-control" placeholder="1001号室など" />
      </div>

      <div className="mb-3">
        <label className="form-label">住所確認書類（必須）</label>
        <input type="file" name="proof_file" className="form-control" required />
        <div className="form-text">
          公共料金の領収書、住民票、請求書などの画像（JPG / PNG / GIF）または PDF / ZIP をアップロードしてください。
        </div>
      </div>

      <button type="submit" className="btn btn-primary" disabled={submitting}>
        {submitting ? "送信中..." : "確認画面へ進む"}
      </button>
    </form>
  );
}
