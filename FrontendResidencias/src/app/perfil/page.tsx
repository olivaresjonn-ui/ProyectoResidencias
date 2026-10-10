"use client";

import { FormEvent, useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { Toast } from "../../components/toast";

type PerfilAlumno = {
  numeroControl: string;
  nombreCompleto: string;
  carrera: string;
  semestre: number;
  estadoResidencia: string;
  habilidadesTecnicas: string | null;
};

export default function PerfilPage() {
  const router = useRouter();
  const [perfil, setPerfil] = useState<PerfilAlumno | null>(null);
  const [habilidades, setHabilidades] = useState("");
  const [cargando, setCargando] = useState(true);
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState("");
  const [aviso, setAviso] = useState("");
  const excedeLimite = habilidades.length > 1000;
  const etiquetas = useMemo(() => habilidades.split(/[,;|]/).map((h) => h.trim()).filter(Boolean), [habilidades]);

  useEffect(() => {
    async function cargarPerfil() {
      const sesionRes = await fetch("/api/auth/me", { cache: "no-store" });
      if (sesionRes.status === 401) { router.replace("/login"); return; }
      if (!sesionRes.ok) throw new Error("No se pudo validar tu sesión. Intenta volver a iniciar sesión.");
      const sesion = await sesionRes.json() as { rol?: string };
      if (sesion.rol !== "ALUMNO") { router.replace("/"); return; }

      const perfilRes = await fetch("/api/alumnos/mio", { cache: "no-store" });
      if (perfilRes.status === 401) { router.replace("/login"); return; }
      if (perfilRes.status === 404) {
        throw new Error("Tu cuenta de alumno no tiene un expediente académico vinculado. Pide a administración que complete tu perfil con tus datos escolares; al guardarlo, también se habilitarán las recomendaciones.");
      }
      if (!perfilRes.ok) throw new Error(`No se pudo cargar tu expediente (HTTP ${perfilRes.status}). Intenta de nuevo en unos minutos.`);
      const datos = await perfilRes.json() as PerfilAlumno;
      setPerfil(datos);
      setHabilidades(datos.habilidadesTecnicas ?? "");
    }

    cargarPerfil()
      .catch((cause: unknown) => setError(cause instanceof Error ? cause.message : "Ocurrió un error inesperado."))
      .finally(() => setCargando(false));
  }, [router]);

  async function guardar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setError(""); setAviso("");
    if (excedeLimite) { setError("Reduce las habilidades a 1000 caracteres o menos."); return; }
    setGuardando(true);
    try {
      const response = await fetch("/api/alumnos/mio/habilidades", {
        method: "PATCH", headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ habilidadesTecnicas: habilidades.trim() }),
      });
      if (response.status === 401) { router.replace("/login"); return; }
      if (!response.ok) { setError("No se pudieron guardar las habilidades. Vuelve a intentarlo."); return; }
      setHabilidades((await response.json() as PerfilAlumno).habilidadesTecnicas ?? "");
      setAviso("Tu perfil quedó actualizado. El recomendador usará estas habilidades.");
    } catch {
      setError("No se pudo contactar al backend. Intenta de nuevo.");
    } finally {
      setGuardando(false);
    }
  }

  return <main className="min-h-screen bg-canvas px-5 py-8 text-ink md:px-10 md:py-12">
    {aviso && <Toast message={aviso} onDismiss={() => setAviso("")} />}
    <div className="mx-auto max-w-4xl"><header className="flex items-center justify-between"><a href="/" className="flex items-center gap-3"><span className="grid h-10 w-10 place-items-center rounded-xl bg-brand font-bold text-white">N</span><span><b className="block">nexo</b><small className="text-slate-500">Mi perfil profesional</small></span></a><a href="/" className="text-sm font-semibold text-brand">← Volver al catálogo</a></header>
      <section className="mt-10 overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-card"><div className="bg-[#172b4d] px-6 py-7 text-white md:px-9"><p className="text-xs font-bold uppercase tracking-[.16em] text-blue-200">Perfil del alumno</p><h1 className="mt-2 text-3xl font-bold">Presenta lo que sabes hacer.</h1><p className="mt-2 max-w-xl text-sm leading-6 text-blue-100/80">Tu carrera y habilidades ayudan a ordenar las vacantes que podrían conectar mejor con tu perfil.</p></div>
        {cargando ? <div className="m-6 h-56 animate-pulse rounded-2xl bg-slate-100"/> : perfil && <div className="grid gap-8 p-6 md:grid-cols-[.8fr_1.2fr] md:p-9"><dl className="space-y-5">{[["Nombre", perfil.nombreCompleto], ["Número de control", perfil.numeroControl], ["Carrera", perfil.carrera], ["Semestre", String(perfil.semestre)], ["Estado de residencia", perfil.estadoResidencia.replaceAll("_", " ")]].map(([label, value]) => <div key={label}><dt className="text-[11px] font-bold uppercase tracking-wide text-slate-400">{label}</dt><dd className="mt-1 text-sm font-semibold">{value}</dd></div>)}</dl>
          <form onSubmit={guardar}><label htmlFor="habilidades" className="block text-sm font-bold">Habilidades técnicas e intereses</label><p className="mt-1 text-xs leading-5 text-slate-500">Escribe tecnologías, herramientas o áreas separadas por coma. Ejemplo: Java, PostgreSQL, análisis de datos.</p><textarea id="habilidades" value={habilidades} maxLength={1200} onChange={(event) => setHabilidades(event.target.value)} rows={7} aria-invalid={excedeLimite} className={`mt-4 w-full resize-y rounded-xl border px-4 py-3 text-sm leading-6 outline-none transition focus:ring-4 ${excedeLimite ? "border-rose-400 focus:border-rose-500 focus:ring-rose-100" : "border-slate-200 focus:border-brand focus:ring-blue-100"}`} placeholder="Java, React, PostgreSQL, diseño de interfaces…"/><div className="mt-1 flex justify-between text-[11px]"><span className={excedeLimite ? "text-rose-600" : "text-slate-400"}>{excedeLimite ? "Superaste el límite de 1000 caracteres." : `${etiquetas.length} ${etiquetas.length === 1 ? "habilidad" : "habilidades"} detectadas`}</span><span className={excedeLimite ? "font-semibold text-rose-600" : "text-slate-400"}>{habilidades.length}/1000</span></div>
            {error && <p role="alert" className="mt-4 rounded-lg bg-rose-50 p-3 text-sm text-rose-700">{error}</p>}<button disabled={guardando || excedeLimite || cargando} className="mt-5 h-11 w-full rounded-xl bg-brand px-4 text-sm font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50">{guardando ? "Guardando…" : "Guardar habilidades"}</button></form></div>}
      </section>
      {!cargando && error && !perfil && <p role="alert" className="mt-4 rounded-xl bg-rose-50 p-4 text-sm text-rose-700">{error}</p>}
      <p className="mt-5 text-xs leading-5 text-slate-400">Solo puedes editar tus habilidades en esta pantalla. La institución administra tu número de control, carrera, semestre y estado.</p>
    </div>
  </main>;
}
