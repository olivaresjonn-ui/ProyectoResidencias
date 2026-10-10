import { NextResponse } from "next/server";
import { proxyBackend } from "../../../../lib/server-proxy";

export const dynamic = "force-dynamic";

async function numeroValido(context: { params: Promise<{ numeroControl: string }> }) {
  const { numeroControl } = await context.params;
  return /^[A-Za-z0-9-]{1,32}$/.test(numeroControl) ? numeroControl : null;
}

export async function GET(_request: Request, context: { params: Promise<{ numeroControl: string }> }) {
  const numero = await numeroValido(context);
  if (!numero) return NextResponse.json({ message: "Número de control inválido." }, { status: 400 });
  return proxyBackend(undefined, `/api/alumnos/${encodeURIComponent(numero)}`, "GET");
}

export async function PUT(request: Request, context: { params: Promise<{ numeroControl: string }> }) {
  const numero = await numeroValido(context);
  if (!numero) return NextResponse.json({ message: "Número de control inválido." }, { status: 400 });
  return proxyBackend(request, `/api/alumnos/${encodeURIComponent(numero)}`, "PUT");
}

export async function DELETE(_request: Request, context: { params: Promise<{ numeroControl: string }> }) {
  const numero = await numeroValido(context);
  if (!numero) return NextResponse.json({ message: "Número de control inválido." }, { status: 400 });
  return proxyBackend(undefined, `/api/alumnos/${encodeURIComponent(numero)}`, "DELETE");
}
