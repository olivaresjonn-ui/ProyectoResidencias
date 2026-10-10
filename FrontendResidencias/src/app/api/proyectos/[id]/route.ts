import { cookies } from "next/headers";
import { NextResponse } from "next/server";

export const dynamic = "force-dynamic";

async function reenviar(request: Request, id: string, method: "PUT" | "DELETE") {
  if (!/^\d+$/.test(id)) return NextResponse.json({ message: "Identificador inválido." }, { status: 400 });
  const token = (await cookies()).get("residencias_token")?.value;
  if (!token) return NextResponse.json({ message: "Inicia sesión para gestionar proyectos." }, { status: 401 });
  const backendBaseUrl = process.env.BACKEND_BASE_URL ?? "http://localhost:8080";

  try {
    const response = await fetch(`${backendBaseUrl}/api/proyectos/${id}`, {
      method,
      headers: {
        Authorization: `Bearer ${token}`,
        ...(method === "PUT" ? { "Content-Type": "application/json" } : {}),
      },
      ...(method === "PUT" ? { body: await request.text() } : {}),
      cache: "no-store",
    });
    const body = await response.text();
    return new NextResponse(body || null, {
      status: response.status,
      headers: { "Content-Type": response.headers.get("content-type") ?? "application/json" },
    });
  } catch {
    return NextResponse.json({ message: "No fue posible contactar al backend Spring Boot." }, { status: 502 });
  }
}

export async function PUT(request: Request, context: { params: Promise<{ id: string }> }) {
  const { id } = await context.params;
  return reenviar(request, id, "PUT");
}

export async function DELETE(request: Request, context: { params: Promise<{ id: string }> }) {
  const { id } = await context.params;
  return reenviar(request, id, "DELETE");
}
