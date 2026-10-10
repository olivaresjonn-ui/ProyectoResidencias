import { cookies } from "next/headers";
import { NextResponse } from "next/server";

export const dynamic = "force-dynamic";

export async function GET() {
  const token = (await cookies()).get("residencias_token")?.value;
  if (!token) return NextResponse.json({ message: "Inicia sesión para ver recomendaciones." }, { status: 401 });
  const backend = process.env.BACKEND_BASE_URL ?? "http://localhost:8080";
  const ml = process.env.ML_BASE_URL ?? "http://localhost:8001";
  try {
    const headers = { Authorization: `Bearer ${token}` };
    const [perfilRes, proyectosRes] = await Promise.all([
      fetch(`${backend}/api/alumnos/mio`, { headers, cache: "no-store" }),
      fetch(`${backend}/api/proyectos`, { headers, cache: "no-store" }),
    ]);
    if (perfilRes.status === 404) return NextResponse.json({ message: "Tu cuenta no tiene un expediente académico vinculado. Administración debe completar tu perfil para generar recomendaciones." }, { status: 404 });
    if (!perfilRes.ok) return NextResponse.json({ message: `No se pudo consultar el perfil de alumno (HTTP ${perfilRes.status}).` }, { status: perfilRes.status });
    if (!proyectosRes.ok) return NextResponse.json({ message: "No se pudieron consultar los proyectos." }, { status: proyectosRes.status });
    const perfil = await perfilRes.json() as { carrera?: string; habilidadesTecnicas?: string };
    const proyectos = await proyectosRes.json() as Array<{ idProyecto: number; titulo: string; descripcion: string; objetivo: string; tecnologiasRequeridas: string; estado: string }>;
    const respuestaML = await fetch(`${ml}/recomendar`, {
      method: "POST", headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ carrera: perfil.carrera ?? "", habilidades: perfil.habilidadesTecnicas ?? "", proyectos: proyectos.filter((p) => p.estado === "DISPONIBLE"), limite: 5 }),
      cache: "no-store",
    });
    if (!respuestaML.ok) return NextResponse.json({ message: "El servicio de recomendaciones no está disponible." }, { status: 502 });
    const datos = await respuestaML.json() as { modelo: string; recomendaciones: Array<{ idProyecto: number; puntaje: number; motivos: string[] }> };
    const porId = new Map(proyectos.map((p) => [p.idProyecto, p]));
    return NextResponse.json({ ...datos, recomendaciones: datos.recomendaciones.map((r) => ({ ...r, proyecto: porId.get(r.idProyecto) })).filter((r) => r.proyecto) });
  } catch {
    return NextResponse.json({ message: "No fue posible conectar con el backend o el servicio ML." }, { status: 502 });
  }
}
