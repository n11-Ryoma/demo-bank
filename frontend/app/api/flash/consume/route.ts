import { cookies } from "next/headers";

const flashCookie = "ebank_flash";

export async function POST() {
  const c = await cookies();
  c.set(flashCookie, "", { httpOnly: true, path: "/", maxAge: 0 });
  const msg = c.get(flashCookie)?.value ?? null;
  // ✅ Route Handler なので delete できる
  if (msg) c.delete(flashCookie);
  return new Response(null, { status: 204 });
}
