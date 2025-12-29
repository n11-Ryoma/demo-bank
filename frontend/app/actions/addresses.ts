"use server";

import { backend } from "@/lib/backend";
import type { AddressChangeResponse, AddressChangeCommitRequest, CurrentAddressResponse } from "@/lib/types";

export async function getCurrentAddress() {
  // OpenAPI上 optional Authorization。ここは「ログインしてたら付ける」でOK
  return backend<CurrentAddressResponse>("/api/address-change/current", { method: "GET", auth: true });
}

function toBase64(buf: ArrayBuffer) {
  return Buffer.from(buf).toString("base64");
}

export async function commitAddressChangeAction(_: unknown, formData: FormData) {
  const postalCode = String(formData.get("postalCode") ?? "");
  const prefecture = String(formData.get("prefecture") ?? "");
  const city = String(formData.get("city") ?? "");
  const addressLine1 = String(formData.get("addressLine1") ?? "");
  const addressLine2 = String(formData.get("addressLine2") ?? "");

  const file = formData.get("proofFile");
  let fileName = "";
  let fileBase64 = "";

  if (file instanceof File) {
    fileName = file.name;
    fileBase64 = toBase64(await file.arrayBuffer());
  }

  const body: AddressChangeCommitRequest = {
    postalCode, prefecture, city, addressLine1, addressLine2,
    fileName, fileBase64
  };

  await backend<AddressChangeResponse>("/api/address-change/commit", { method: "POST", body, auth: true });
}
