import { NextResponse } from "next/server";

export const dynamic = "force-dynamic";

export async function POST(request: Request) {
  const backendBaseUrl = process.env.BACKEND_BASE_URL ?? "http://localhost:8080";

  try {
    const payload: unknown = await request.json();
    if (
      !payload ||
      typeof payload !== "object" ||
      !("correo" in payload) ||
      typeof payload.correo !== "string" ||
      !("password" in payload) ||
      typeof payload.password !== "string"
    ) {
      return NextResponse.json({ message: "Correo y contraseña son obligatorios." }, { status: 400 });
    }

    const response = await fetch(`${backendBaseUrl}/api/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ correo: payload.correo, password: payload.password }),
      cache: "no-store",
    });
    const body = await response.text();

    if (!response.ok) {
      return new NextResponse(body, {
        status: response.status,
        headers: { "Content-Type": response.headers.get("content-type") ?? "text/plain" },
      });
    }

    const auth = JSON.parse(body) as {
      accessToken: string;
      expiresInSeconds: number;
      usuario: { idUsuario: number; correo: string; rol: string };
    };
    const nextResponse = NextResponse.json({ usuario: auth.usuario });
    nextResponse.cookies.set("residencias_token", auth.accessToken, {
      httpOnly: true,
      secure: process.env.NODE_ENV === "production",
      sameSite: "lax",
      path: "/",
      maxAge: auth.expiresInSeconds,
    });
    return nextResponse;
  } catch {
    return NextResponse.json({ message: "No fue posible contactar al backend Spring Boot." }, { status: 502 });
  }
}
