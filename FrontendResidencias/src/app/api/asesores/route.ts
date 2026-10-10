import { cookies } from "next/headers";
import { NextResponse } from "next/server";
import { proxyBackend } from "../../../lib/server-proxy";

export const dynamic = "force-dynamic";

export async function GET() {
  const token = (await cookies()).get("residencias_token")?.value;
  if (!token) return NextResponse.json({ message: "Inicia sesión para consultar asesores." }, { status: 401 });
  const backendBaseUrl = process.env.BACKEND_BASE_URL ?? "http://localhost:8080";
  try {
    const response = await fetch(`${backendBaseUrl}/api/asesores`, {
      headers: { Authorization: `Bearer ${token}` },
      cache: "no-store",
    });
    return new NextResponse(await response.text(), {
      status: response.status,
      headers: { "Content-Type": response.headers.get("content-type") ?? "application/json" },
    });
  } catch {
    return NextResponse.json({ message: "No fue posible contactar al backend Spring Boot." }, { status: 502 });
  }
}

export async function POST(request: Request) {
  return proxyBackend(request, "/api/asesores", "POST");
}
