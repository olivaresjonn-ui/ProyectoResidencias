import { NextResponse } from "next/server";
import { cookies } from "next/headers";

export const dynamic = "force-dynamic";

export async function GET() {
  const backendBaseUrl = process.env.BACKEND_BASE_URL ?? "http://localhost:8080";
  const token = (await cookies()).get("residencias_token")?.value;

  if (!token) {
    return NextResponse.json({ message: "Inicia sesión para consultar los proyectos." }, { status: 401 });
  }

  try {
    const response = await fetch(`${backendBaseUrl}/api/proyectos`, {
      cache: "no-store",
      headers: { Authorization: `Bearer ${token}` },
    });
    const body = await response.text();

    return new NextResponse(body, {
      status: response.status,
      headers: { "Content-Type": response.headers.get("content-type") ?? "application/json" },
    });
  } catch {
    return NextResponse.json(
      { message: "No fue posible contactar al backend Spring Boot." },
      { status: 502 },
    );
  }
}

export async function POST(request: Request) {
  const backendBaseUrl = process.env.BACKEND_BASE_URL ?? "http://localhost:8080";
  const token = (await cookies()).get("residencias_token")?.value;
  if (!token) return NextResponse.json({ message: "Inicia sesión para publicar un proyecto." }, { status: 401 });

  try {
    const response = await fetch(`${backendBaseUrl}/api/proyectos`, {
      method: "POST",
      headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" },
      body: await request.text(),
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
