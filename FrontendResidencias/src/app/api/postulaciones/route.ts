import { cookies } from "next/headers";
import { NextResponse } from "next/server";

export const dynamic = "force-dynamic";

export async function GET() {
  const token = (await cookies()).get("residencias_token")?.value;
  if (!token) return NextResponse.json({ message: "Inicia sesión para consultar las postulaciones." }, { status: 401 });
  const backendBaseUrl = process.env.BACKEND_BASE_URL ?? "http://localhost:8080";
  try {
    const response = await fetch(`${backendBaseUrl}/api/postulaciones`, {
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
  const token = (await cookies()).get("residencias_token")?.value;
  if (!token) return NextResponse.json({ message: "Inicia sesión para postularte." }, { status: 401 });

  const backendBaseUrl = process.env.BACKEND_BASE_URL ?? "http://localhost:8080";
  try {
    const response = await fetch(`${backendBaseUrl}/api/postulaciones`, {
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
