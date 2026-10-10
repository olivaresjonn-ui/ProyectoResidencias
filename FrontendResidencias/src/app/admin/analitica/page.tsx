"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

type Analitica = {
  proyectosRegistrados: number;
  vacantesAbiertas: number;
  totalPostulaciones: number;
  tecnologiasDistintas: number;
  tecnologias: { nombre: string; cantidad: number }[];
  postulacionesPorEstado: Record<string, number>;
};

const estadosPostulacion = [
  { key: "EN_REVISION", label: "En revisión", color: "bg-amber-500" },
  { key: "ACEPTADA", label: "Aceptadas", color: "bg-emerald-500" },
  { key: "RECHAZADA", label: "Rechazadas", color: "bg-rose-400" },
] as const;

export default function AnaliticaPage() {
  const router = useRouter();
  const [datos, setDatos] = useState<Analitica | null>(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");

  async function cargar() {
    const [sesionRes, analiticaRes] = await Promise.all([
      fetch("/api/auth/me", { cache: "no-store" }),
      fetch("/api/analitica", { cache: "no-store" }),
    ]);
    if ([sesionRes, analiticaRes].some((response) => response.status === 401)) {
      router.replace("/login"); return;
    }
    const sesion = await sesionRes.json() as { rol?: string };
    if (sesion.rol !== "ADMIN") { router.replace("/"); return; }
    if (!analiticaRes.ok) throw new Error("No se pudieron cargar las cifras institucionales.");
    setDatos(await analiticaRes.json() as Analitica);
  }

  useEffect(() => {
    cargar().catch((cause: unknown) => setError(cause instanceof Error ? cause.message : "Error de conexión."))
      .finally(() => setCargando(false));
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const tecnologias = datos?.tecnologias ?? [];
  const abiertas = datos?.vacantesAbiertas ?? 0;
  const totalAspirantes = datos?.totalPostulaciones ?? 0;
  const maxDemanda = tecnologias[0]?.cantidad ?? 1;

  return <main className="min-h-screen bg-canvas px-5 py-8 text-ink md:px-10 md:py-10"><div className="mx-auto max-w-7xl">
    <header className="flex flex-wrap items-center justify-between gap-4"><a href="/admin/asignaciones" className="flex items-center gap-3"><span className="grid h-10 w-10 place-items-center rounded-xl bg-brand font-bold text-white">N</span><span><b className="block">nexo · Administración</b><small className="text-slate-500">Indicadores institucionales</small></span></a><div className="flex flex-wrap gap-2"><a href="/admin/asignaciones" className="rounded-lg border border-slate-200 bg-white px-3 py-2 text-xs font-semibold text-slate-600">Asignar asesores</a><a href="/admin/empresas" className="rounded-lg border border-slate-200 bg-white px-3 py-2 text-xs font-semibold text-slate-600">Empresas</a><a href="/admin/cuentas" className="rounded-lg border border-slate-200 bg-white px-3 py-2 text-xs font-semibold text-slate-600">Cuentas</a><button onClick={() => { setCargando(true); setError(""); cargar().catch((cause: unknown) => setError(cause instanceof Error ? cause.message : "Error de conexión.")).finally(() => setCargando(false)); }} className="rounded-lg bg-brand px-3 py-2 text-xs font-semibold text-white">Actualizar cifras ↻</button><button onClick={async () => { await fetch("/api/auth/logout", { method: "POST" }); router.replace("/login"); }} className="rounded-lg px-3 py-2 text-xs font-semibold text-slate-500">Cerrar sesión</button></div></header>
    <div className="mt-10"><p className="text-xs font-bold uppercase tracking-[.16em] text-brand">Demanda del mercado</p><h1 className="mt-2 text-3xl font-bold tracking-tight">Analítica de residencias</h1><p className="mt-2 max-w-2xl text-sm leading-6 text-slate-500">Resumen agregado de las vacantes publicadas y las tecnologías que las empresas solicitan con más frecuencia.</p></div>
    {error && <p role="alert" className="mt-6 rounded-xl bg-rose-50 p-4 text-sm text-rose-700">{error}</p>}
    <section className="mt-7 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">{[["Proyectos registrados", datos?.proyectosRegistrados ?? 0, "en el catálogo institucional"], ["Vacantes abiertas", abiertas, "estado disponible"], ["Postulaciones", totalAspirantes, "solicitudes recibidas"], ["Tecnologías identificadas", datos?.tecnologiasDistintas ?? 0, "términos distintos"]].map(([label, cantidad, detalle]) => <article key={String(label)} className="rounded-2xl border border-slate-200 bg-white p-5 shadow-card"><p className="text-sm text-slate-500">{label}</p><p className="mt-3 text-3xl font-bold">{cargando ? "—" : cantidad}</p><p className="mt-1 text-xs text-slate-400">{detalle}</p></article>)}</section>
    <section className="mt-6 grid items-start gap-6 xl:grid-cols-[1.25fr_.75fr]"><article className="rounded-2xl border border-slate-200 bg-white p-5 shadow-card md:p-6"><div className="flex items-end justify-between gap-4"><div><h2 className="text-lg font-bold">Tecnologías más solicitadas</h2><p className="mt-1 text-xs text-slate-500">Cantidad de proyectos que mencionan cada tecnología o competencia.</p></div><span className="rounded-lg bg-blue-50 px-2.5 py-1.5 text-[10px] font-bold text-brand">POR PROYECTO</span></div>
      {cargando ? <div className="mt-7 h-64 animate-pulse rounded-xl bg-slate-100"/> : tecnologias.length === 0 ? <p className="mt-6 rounded-xl border border-dashed border-slate-200 p-8 text-center text-sm text-slate-500">Todavía no hay tecnologías publicadas para analizar.</p> : <div className="mt-7 space-y-4">{tecnologias.map((tecnologia) => <div key={tecnologia.nombre}><div className="mb-1.5 flex justify-between gap-3 text-xs"><span className="font-semibold">{tecnologia.nombre}</span><span className="text-slate-500">{tecnologia.cantidad} {tecnologia.cantidad === 1 ? "proyecto" : "proyectos"}</span></div><div className="h-2.5 overflow-hidden rounded-full bg-slate-100"><div className="h-full rounded-full bg-gradient-to-r from-brand to-sky-400 transition-all duration-500" style={{ width: `${(tecnologia.cantidad / maxDemanda) * 100}%` }}/></div></div>)}</div>}
    </article>
    <article className="rounded-2xl border border-slate-200 bg-white p-5 shadow-card md:p-6"><h2 className="text-lg font-bold">Estado de postulaciones</h2><p className="mt-1 text-xs text-slate-500">Distribución del flujo de selección actual.</p>{cargando ? <div className="mt-6 h-40 animate-pulse rounded-xl bg-slate-100"/> : <div className="mt-6 space-y-5">{estadosPostulacion.map((estado) => { const cantidad = datos?.postulacionesPorEstado[estado.key] ?? 0; const porcentaje = totalAspirantes ? (cantidad / totalAspirantes) * 100 : 0; return <div key={estado.key}><div className="mb-1.5 flex justify-between text-xs"><span className="font-medium text-slate-600">{estado.label}</span><span className="font-bold">{cantidad} <span className="font-normal text-slate-400">({Math.round(porcentaje)}%)</span></span></div><div className="h-2 overflow-hidden rounded-full bg-slate-100"><div className={`h-full rounded-full ${estado.color} transition-all duration-500`} style={{ width: `${porcentaje}%` }}/></div></div>; })}</div>}
      <p className="mt-7 rounded-xl bg-slate-50 p-3 text-[11px] leading-5 text-slate-500">Las cifras se calculan al momento de consultar, a partir de los proyectos y postulaciones visibles para administración. Cada tecnología cuenta una vez por proyecto.</p>
    </article></section>
    <footer className="mt-8 text-xs text-slate-400">Nexo · Datos agregados, sin mostrar información personal de alumnos.</footer>
  </div></main>;
}
