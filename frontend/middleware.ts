import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

const cookieName = process.env.JWT_COOKIE_NAME ?? "ebank_token";

export function middleware(req: NextRequest) {
  const { pathname } = req.nextUrl;

  const protectedPaths = ["/dashboard", "/transfer", "/transactions", "/address"];
  const isProtected = protectedPaths.some((p) => pathname === p || pathname.startsWith(p + "/"));
  if (!isProtected) return NextResponse.next();

  const token = req.cookies.get(cookieName)?.value;
  if (!token) {
    const url = req.nextUrl.clone();
    url.pathname = "/login";
    return NextResponse.redirect(url);
  }

  return NextResponse.next();
}

export const config = {
  matcher: ["/dashboard", "/transfer", "/transactions", "/address"]
};
