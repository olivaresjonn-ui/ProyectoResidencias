"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { Toast } from "../../../components/toast";

type Empresa = {
  idEmpresa: number;
  razonSocial: string;
  sector: string;
  nombreCompleto: string;
  telefono: string;
  direccion: string;
  estadoVerificacion: "PENDIENTE" | "VERIFICADA" | "RECHAZADA";
};

const etiquetas: Record<Empresa["estadoVerificacion"], string> = {
  PENDIENTE: "Pendiente",
  VERIFICADA: "Verificada",
  RECHAZADA: "Rechazada",
};

export default function VerificarEmpresasPage() {
  const router = useRouter();
  const [empresas, setEmpresas] = useState<Empresa[]>([]);
  const [cargando, setCargando] = useState(true);
  const [actualizando, setActualizando] = useState<number | null>(null);
  const [error, setError] = useState("");
  const [aviso, setAviso] = useState("");

  async function cargar() {
    const [sesionRes, empresasRes] = await Promise.all([
      fetch("/api/auth/me", { cache: "no-store" }),
      fetch("/api/empresas", { cache: "no-store" }),
    ]);
    if (sesionRes.status === 401 || empresasRes.status === 401) { router.replace("/login"); return; }
    const sesion = await sesionRes.json() as { rol?: string };
    if (sesion.rol !== "ADMIN") { router.replace("/"); return; }
    if (!empresasRes.ok) throw new Error("No se pudieron consultar las empresas.");
    setEmpresas(await empresasRes.json() as Empresa[]);
  }

  useEffect(() => {
    cargar().catch((cause: unknown) => setError(cause instanceof Error ? cause.message : "Error de conexión."))
      .finally(() => setCargando(false));
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  async function revisar(empresa: Empresa, estadoVerificacion: Empresa["estadoVerificacion"]) {
    setError(""); setAviso(""); setActualizando(empresa.idEmpresa);
    try {
      const response = await fetch(`/api/empresas/${empresa.idEmpresa}/verificacion`, {
        method: "PATCH", headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ estadoVerificacion }),
      });
      if (response.status === 401) { router.replace("/login"); return; }
      if (!response.ok) { setError("No se pudo actualizar la verificación. Vuelve a intentarlo."); return; }
      setAviso(estadoVerificacion === "PENDIENTE"
        ? `Se reabrió la revisión de ${empresa.razonSocial}.`
        : `${empresa.razonSocial}: estado ${etiquetas[estadoVerificacion].toLowerCase()}.`);
      await cargar();
    } catch {
      setError("No se pudo contactar al backend Spring Boot.");
    } finally {
      setActualizando(null);
    }
  }

  const pendientes = empresas.filter((empresa) => empresa.estadoVerificacion === "PENDIENTE").length;
  const verificadas = empresas.filter((empresa) => empresa.estadoVerificacion === "VERIFICADA").length;
  return <main className="min-h-screen bg-canvas px-5 py-8 text-ink md:px-10 md:py-10">
    {aviso && <Toast message={aviso} onDismiss={() => setAviso("")} />}
    <div className="mx-auto max-w-7xl"><header className="flex flex-wrap items-center justify-between gap-4"><a href="/admin/asignaciones" className="flex items-center gap-3"><span className="grid h-10 w-10 place-items-center rounded-xl bg-brand font-bold text-white">N</span><span><b className="block">nexo · Administración</b><small className="text-slate-500">Revisión de empresas</small></span></a><nav className="flex flex-wrap gap-2"><a href="/admin/asignaciones" className="rounded-lg border border-slate-200 bg-white px-3 py-2 text-xs font-semibold text-slate-600">Asesores</a><a href="/admin/analitica" className="rounded-lg border border-slate-200 bg-white px-3 py-2 text-xs font-semibold text-slate-600">Analítica</a><a href="/admin/cuentas" className="rounded-lg border border-slate-200 bg-white px-3 py-2 text-xs font-semibold text-slate-600">Cuentas</a><button onClick={async () => { await fetch("/api/auth/logout", { method: "POST" }); router.replace("/login"); }} className="px-3 py-2 text-xs font-semibold text-slate-500">Cerrar sesión</button></nav></header>
      <div className="mt-10"><p className="text-xs font-bold uppercase tracking-[.16em] text-brand">Habilitar vacantes</p><h1 className="mt-2 text-3xl font-bold tracking-tight">Verificación de empresas</h1><p className="mt-2 max-w-2xl text-sm leading-6 text-slate-500">Revisa los datos de registro. Solo las empresas verificadas pueden publicar proyectos de residencia.</p></div>
      {error && <p role="alert" className="mt-5 rounded-xl bg-rose-50 p-4 text-sm text-rose-700">{error}</p>}
      <section className="mt-6 grid gap-4 sm:grid-cols-3">{[["Empresas registradas", empresas.length], ["Por revisar", pendientes], ["Verificadas", verificadas]].map(([label, valor]) => <article key={String(label)} className="rounded-2xl border border-slate-200 bg-white p-5"><p className="text-sm text-slate-500">{label}</p><p className="mt-2 text-3xl font-bold">{cargando ? "—" : valor}</p></article>)}</section>
      <section className="mt-6 overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-card"><div className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-100 px-5 py-4"><div><h2 className="font-bold">Directorio de empresas</h2><p className="mt-1 text-xs text-slate-500">La revisión solo cambia el estado de verificación del perfil.</p></div><a href="/admin/analitica" className="text-xs font-semibold text-brand">Ver demanda de tecnologías →</a></div>
        {cargando ? <div className="m-5 h-52 animate-pulse rounded-xl bg-slate-100"/> : empresas.length === 0 ? <p className="p-10 text-center text-sm text-slate-500">No hay empresas registradas.</p> : <div className="divide-y divide-slate-100">{empresas.map((empresa) => <article key={empresa.idEmpresa} className="flex flex-col justify-between gap-4 px-5 py-5 lg:flex-row lg:items-center"><div className="min-w-0"><div className="flex flex-wrap items-center gap-2"><h3 className="font-bold">{empresa.razonSocial}</h3><span className={`rounded-full px-2.5 py-1 text-[10px] font-semibold ${empresa.estadoVerificacion === "VERIFICADA" ? "bg-emerald-50 text-emerald-700" : empresa.estadoVerificacion === "RECHAZADA" ? "bg-rose-50 text-rose-700" : "bg-amber-50 text-amber-700"}`}>{etiquetas[empresa.estadoVerificacion]}</span></div><p className="mt-1 text-xs text-slate-500">{empresa.sector} · Contacto: {empresa.nombreCompleto}</p><p className="mt-1 text-xs text-slate-400">{empresa.telefono} · {empresa.direccion}</p></div>{empresa.estadoVerificacion === "PENDIENTE" && <div className="flex shrink-0 gap-2"><button disabled={actualizando === empresa.idEmpresa} onClick={() => revisar(empresa, "RECHAZADA")} className="rounded-lg border border-rose-200 px-3 py-2 text-xs font-semibold text-rose-700 hover:bg-rose-50 disabled:opacity-50">Rechazar</button><button disabled={actualizando === empresa.idEmpresa} onClick={() => revisar(empresa, "VERIFICADA")} className="rounded-lg bg-emerald-600 px-3 py-2 text-xs font-semibold text-white hover:bg-emerald-700 disabled:opacity-50">{actualizando === empresa.idEmpresa ? "Guardando…" : "Verificar empresa"}</button></div>}{empresa.estadoVerificacion === "RECHAZADA" && <button disabled={actualizando === empresa.idEmpresa} onClick={() => revisar(empresa, "PENDIENTE")} className="w-fit rounded-lg border border-slate-200 px-3 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-50 disabled:opacity-50">Reabrir revisión</button>}</article>)}</div>}
      </section><p className="mt-4 text-xs text-slate-400">Los perfiles rechazados pueden reabrirse para una nueva revisión.</p>
    </div>
  </main>;
}
