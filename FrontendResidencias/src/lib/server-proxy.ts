import { cookies } from "next/headers";
import { NextResponse } from "next/server";

export async function proxyBackend(request: Request | undefined, path: string, method: "GET" | "POST" | "PUT" | "DELETE") {
  const token = (await cookies()).get("residencias_token")?.value;
  if (!token) return NextResponse.json({ message: "Inicia sesión para continuar." }, { status: 401 });
  const base = process.env.BACKEND_BASE_URL ?? "http://localhost:8080";
  try {
    const headers = new Headers({ Authorization: `Bearer ${token}` });
    let body: string | undefined;
    if (method === "POST" || method === "PUT") {
      headers.set("Content-Type", "application/json");
      body = request ? await request.text() : undefined;
    }
    const response = await fetch(`${base}${path}`, { method, headers, body, cache: "no-store" });
    return new NextResponse(await response.text(), {
      status: response.status,
      headers: { "Content-Type": response.headers.get("content-type") ?? "application/json" },
    });
  } catch {
    return NextResponse.json({ message: "No fue posible contactar al backend Spring Boot." }, { status: 502 });
  }
}
