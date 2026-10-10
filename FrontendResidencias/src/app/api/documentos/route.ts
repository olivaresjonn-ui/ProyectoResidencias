import { cookies } from "next/headers";
import { NextResponse } from "next/server";

export const dynamic = "force-dynamic";

function respuesta(response: Response, body: BodyInit | null) {
  return new NextResponse(body, {
    status: response.status,
    headers: { "Content-Type": response.headers.get("content-type") ?? "application/json" },
  });
}

export async function GET() {
  const token = (await cookies()).get("residencias_token")?.value;
  if (!token) return NextResponse.json({ message: "Inicia sesión para consultar tus documentos." }, { status: 401 });
  const backendBaseUrl = process.env.BACKEND_BASE_URL ?? "http://localhost:8080";
  try {
    const response = await fetch(`${backendBaseUrl}/api/documentos`, {
      headers: { Authorization: `Bearer ${token}` },
      cache: "no-store",
    });
    return respuesta(response, await response.text());
  } catch {
    return NextResponse.json({ message: "No fue posible contactar al backend Spring Boot." }, { status: 502 });
  }
}

export async function POST(request: Request) {
  const token = (await cookies()).get("residencias_token")?.value;
  if (!token) return NextResponse.json({ message: "Inicia sesión para subir documentos." }, { status: 401 });
  const backendBaseUrl = process.env.BACKEND_BASE_URL ?? "http://localhost:8080";
  try {
    const formulario = await request.formData();
    const response = await fetch(`${backendBaseUrl}/api/documentos`, {
      method: "POST",
      headers: { Authorization: `Bearer ${token}` },
      body: formulario,
      cache: "no-store",
    });
    return respuesta(response, await response.text());
  } catch {
    return NextResponse.json({ message: "No fue posible contactar al backend Spring Boot." }, { status: 502 });
  }
}
