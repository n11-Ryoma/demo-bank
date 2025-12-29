import Link from "next/link";
import Image from "next/image";
import { getUsername } from "@/lib/backend";
import { logoutAction } from "@/app/actions/auth";

type Active = "dashboard" | "transactions" | "address";

export default async function AppShell({
  children,
  active,
}: {
  children: React.ReactNode;
  active: Active;
}) {
  const username = await getUsername();

  return (
    <>
      {/* Top Navbar */}
      <nav className="navbar navbar-expand-lg navbar-dark bg-primary mb-0">
        <div className="container-fluid">
          <Link className="navbar-brand d-flex align-items-center" href="/dashboard">
            {/* public/images/logo.png を置けば表示される */}
            <Image
              src="/images/logo.png"
              alt="eBank Logo"
              width={200}
              height={100}
              className="brand-logo"
              priority
            />
            <span className="fw-bold">+Acts Bank</span>
          </Link>

          <div className="d-flex align-items-center">
            <span className="navbar-text me-3 text-white">
              {username ? `${username} さん` : ""}
            </span>
            <form action={logoutAction}>
              <button className="btn btn-light btn-sm" type="submit">
                ログアウト
              </button>
            </form>
          </div>
        </div>
      </nav>

      {/* Body */}
      <div className="container-fluid">
        <div className="row">
          {/* Sidebar */}
          <nav className="col-md-3 col-lg-2 d-md-block bg-white border-end sidebar p-3">
            <h6 className="text-muted small">メニュー</h6>
            <ul className="nav flex-column">
              <li className="nav-item">
                <Link className={`nav-link ${active === "dashboard" ? "active fw-bold" : ""}`} href="/dashboard">
                  残高照会
                </Link>
              </li>
              <li className="nav-item">
                <Link className={`nav-link ${active === "transactions" ? "active fw-bold" : ""}`} href="/transactions">
                  取引明細
                </Link>
              </li>
              <li className="nav-item">
                <Link className="nav-link" href="/dashboard#transfer">
                  振込
                </Link>
              </li>
              <li className="nav-item">
                <Link className={`nav-link ${active === "address" ? "active fw-bold" : ""}`} href="/address">
                  住所変更
                </Link>
              </li>
            </ul>
          </nav>

          {/* Main */}
          <main className="col-md-9 col-lg-10 p-4">{children}</main>
        </div>
      </div>
    </>
  );
}
