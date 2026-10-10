import { NextResponse } from "next/server";
import { proxyBackend } from "../../../../lib/server-proxy";

export const dynamic = "force-dynamic";

async function idValido(context: { params: Promise<{ id: string }> }) {
  const { id } = await context.params;
  return /^\d+$/.test(id) ? id : null;
}

export async function GET(_request: Request, context: { params: Promise<{ id: string }> }) {
  const id = await idValido(context);
  if (!id) return NextResponse.json({ message: "Identificador inválido." }, { status: 400 });
  return proxyBackend(undefined, `/api/asesores/${id}`, "GET");
}

export async function PUT(request: Request, context: { params: Promise<{ id: string }> }) {
  const id = await idValido(context);
  if (!id) return NextResponse.json({ message: "Identificador inválido." }, { status: 400 });
  return proxyBackend(request, `/api/asesores/${id}`, "PUT");
}

export async function DELETE(_request: Request, context: { params: Promise<{ id: string }> }) {
  const id = await idValido(context);
  if (!id) return NextResponse.json({ message: "Identificador inválido." }, { status: 400 });
  return proxyBackend(undefined, `/api/asesores/${id}`, "DELETE");
}
