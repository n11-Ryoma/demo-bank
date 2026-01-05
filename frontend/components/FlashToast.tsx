"use client";

import { useEffect, useRef } from "react";

export default function FlashToast({
  message,
  variant = "danger",
}: {
  message: string | null;
  variant?: "success" | "danger";
}) {
  const consumedRef = useRef(false);

  useEffect(() => {
    if (!message) return;
    if (consumedRef.current) return; // ← 二重実行ガード
    consumedRef.current = true;

    fetch("/api/flash/consume", { method: "POST" }).catch(() => {});
  }, [message]);

  if (!message) return null;

  return (
    <div className={`alert alert-${variant}`} role="alert">
      {message}
    </div>
  );
}
