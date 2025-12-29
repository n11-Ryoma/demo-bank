import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "eBank",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="ja">
      <head>
        <link
          href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet"
        />
      </head>
      <body className="bg-light">{children}</body>
    </html>
  );
}
