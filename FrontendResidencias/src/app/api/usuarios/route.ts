import { proxyBackend } from "../../../lib/server-proxy";

export const dynamic = "force-dynamic";

export async function GET() {
  return proxyBackend(undefined, "/api/usuarios", "GET");
}

export async function POST(request: Request) {
  return proxyBackend(request, "/api/usuarios", "POST");
}
