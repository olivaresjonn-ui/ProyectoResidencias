import { proxyBackend } from "../../../lib/server-proxy";

export const dynamic = "force-dynamic";

export async function GET() {
  return proxyBackend(undefined, "/api/analitica", "GET");
}
