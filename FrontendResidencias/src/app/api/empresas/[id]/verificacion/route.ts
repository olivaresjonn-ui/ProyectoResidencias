import { cookies } from "next/headers";
import { NextResponse } from "next/server";

export const dynamic = "force-dynamic";

export async function PATCH(request: Request, context: { params: Promise<{ id: string }> }) {
  const { id } = await context.params;
  if (!/^\d+$/.test(id)) return NextResponse.json({ message: "Identificador inválido." }, { status: 400 });
  const token = (await cookies()).get("residencias_token")?.value;
  if (!token) return NextResponse.json({ message: "Inicia sesión para revisar la empresa." }, { status: 401 });
  const base = process.env.BACKEND_BASE_URL ?? "http://localhost:8080";
  try {
    const response = await fetch(`${base}/api/empresas/${id}/verificacion`, {
      method: "PATCH", headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" },
      body: await request.text(), cache: "no-store",
    });
    return new NextResponse(await response.text(), { status: response.status, headers: { "Content-Type": response.headers.get("content-type") ?? "application/json" } });
  } catch {
    return NextResponse.json({ message: "No fue posible contactar al backend Spring Boot." }, { status: 502 });
  }
}
