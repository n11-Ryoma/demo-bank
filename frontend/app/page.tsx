import { cookies } from "next/headers";
import { redirect } from "next/navigation";

const cookieName = process.env.JWT_COOKIE_NAME ?? "ebank_token";

export default async function Home() {
  const c = await cookies();
  const token = c.get(cookieName)?.value;

  redirect(token ? "/dashboard" : "/login");
}
