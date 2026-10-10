import { NextResponse } from "next/server";
import { proxyBackend } from "../../../../lib/server-proxy";

export const dynamic = "force-dynamic";

export async function GET(_request: Request, context: { params: Promise<{ id: string }> }) {
  const { id } = await context.params;
  if (!/^\d+$/.test(id)) return NextResponse.json({ message: "Identificador inválido." }, { status: 400 });
  return proxyBackend(undefined, `/api/usuarios/${id}`, "GET");
}

export async function PUT(request: Request, context: { params: Promise<{ id: string }> }) {
  const { id } = await context.params;
  if (!/^\d+$/.test(id)) return NextResponse.json({ message: "Identificador inválido." }, { status: 400 });
  return proxyBackend(request, `/api/usuarios/${id}`, "PUT");
}

export async function DELETE(_request: Request, context: { params: Promise<{ id: string }> }) {
  const { id } = await context.params;
  if (!/^\d+$/.test(id)) return NextResponse.json({ message: "Identificador inválido." }, { status: 400 });
  return proxyBackend(undefined, `/api/usuarios/${id}`, "DELETE");
}
