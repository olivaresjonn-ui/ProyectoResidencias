"use client";

import { FormEvent, useCallback, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import type { Proyecto } from "../../../lib/api";
import { Toast } from "../../../components/toast";

type FormProyecto = {
  codigoProyecto: string;
  titulo: string;
  descripcion: string;
  objetivo: string;
  tecnologiasRequeridas: string;
  cupoAlumnos: string;
};

type CampoProyecto = keyof FormProyecto;

function validarCampo(campo: CampoProyecto, valor: string) {
  const texto = valor.trim();
  if (!texto) return "Este campo es obligatorio.";
  if (campo === "codigoProyecto" && !/^[a-zA-Z0-9][a-zA-Z0-9_-]{2,39}$/.test(valor)) return "Usa de 3 a 40 caracteres, sin espacios: letras, números, guion o guion bajo.";
  if (campo === "titulo" && texto.length < 5) return "Escribe al menos 5 caracteres.";
  if (campo === "descripcion" && texto.length < 20) return "Describe el proyecto con al menos 20 caracteres.";
  if (campo === "objetivo" && texto.length < 10) return "Especifica el resultado esperado con al menos 10 caracteres.";
  if (campo === "tecnologiasRequeridas" && texto.length < 2) return "Añade al menos una tecnología o competencia.";
  if (campo === "cupoAlumnos") {
    const cupo = Number(texto);
    if (!Number.isInteger(cupo) || cupo < 1 || cupo > 100) return "El cupo debe ser un entero entre 1 y 100.";
  }
  return "";
}

const formularioInicial: FormProyecto = {
  codigoProyecto: "",
  titulo: "",
  descripcion: "",
  objetivo: "",
  tecnologiasRequeridas: "",
  cupoAlumnos: "1",
};

export default function ProyectosEmpresaPage() {
  const router = useRouter();
  const [proyectos, setProyectos] = useState<Proyecto[]>([]);
  const [formulario, setFormulario] = useState<FormProyecto>(formularioInicial);
  const [cargando, setCargando] = useState(true);
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState("");
  const [aviso, setAviso] = useState("");
  const [tocados, setTocados] = useState<Partial<Record<CampoProyecto, boolean>>>({});

  const erroresCampos = Object.fromEntries(
    (Object.keys(formulario) as CampoProyecto[]).map((campo) => [campo, validarCampo(campo, formulario[campo])]),
  ) as Record<CampoProyecto, string>;

  function actualizarCampo(campo: CampoProyecto, valor: string) {
    setFormulario((actual) => ({ ...actual, [campo]: valor }));
    setTocados((actual) => ({ ...actual, [campo]: true }));
  }

  const cargarProyectos = useCallback(async () => {
    const [resSesion, resProyectos] = await Promise.all([
      fetch("/api/auth/me", { cache: "no-store" }),
      fetch("/api/proyectos/mios", { cache: "no-store" }),
    ]);
    if (resSesion.status === 401 || resProyectos.status === 401) {
      router.replace("/login");
      return;
    }
    if (!resSesion.ok || !resProyectos.ok) throw new Error("No se pudo cargar el espacio de empresa.");
    const sesion = await resSesion.json() as { rol: string };
    if (sesion.rol !== "EMPRESA") {
      router.replace("/");
      return;
    }
    setProyectos(await resProyectos.json() as Proyecto[]);
  }, [router]);

  useEffect(() => {
    cargarProyectos()
      .catch((cause: unknown) => setError(cause instanceof Error ? cause.message : "Ocurrió un error inesperado."))
      .finally(() => setCargando(false));
  }, [cargarProyectos]);

  async function crearProyecto(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setAviso("");
    setTocados(Object.fromEntries((Object.keys(formulario) as CampoProyecto[]).map((campo) => [campo, true])));
    if (Object.values(erroresCampos).some(Boolean)) {
      setError("Corrige los campos señalados antes de publicar.");
      return;
    }
    setGuardando(true);
    try {
      const response = await fetch("/api/proyectos", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ ...formulario, cupoAlumnos: Number(formulario.cupoAlumnos) }),
      });
      if (response.status === 401) return router.replace("/login");
      if (response.status === 403) {
        setError("Tu empresa debe estar verificada por la institución antes de publicar proyectos.");
        return;
      }
      if (response.status === 409) {
        setError("Ese código de proyecto ya existe. Elige otro código.");
        return;
      }
      if (!response.ok) {
        setError("No se pudo publicar. Revisa que los campos estén completos y que el cupo sea mayor que cero.");
        return;
      }
      setFormulario(formularioInicial);
      setTocados({});
      setAviso("Proyecto publicado. Ya aparece en el catálogo de oportunidades.");
      await cargarProyectos();
    } catch {
      setError("No se pudo contactar al backend. Intenta de nuevo.");
    } finally {
      setGuardando(false);
    }
  }

  async function cambiarEstado(proyecto: Proyecto) {
    const nuevoEstado = proyecto.estado === "DISPONIBLE" ? "CERRADO" : "DISPONIBLE";
    const response = await fetch(`/api/proyectos/${proyecto.idProyecto}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        titulo: proyecto.titulo,
        descripcion: proyecto.descripcion,
        objetivo: proyecto.objetivo,
        tecnologiasRequeridas: proyecto.tecnologiasRequeridas,
        cupoAlumnos: proyecto.cupoAlumnos,
        estado: nuevoEstado,
      }),
    });
    if (!response.ok) {
      setError(response.status === 404 ? "No se encontró el proyecto dentro de tu empresa." : "No se pudo cambiar el estado del proyecto.");
      return;
    }
    setAviso(nuevoEstado === "CERRADO" ? "Proyecto cerrado." : "Proyecto publicado de nuevo.");
    await cargarProyectos();
  }

  async function eliminarProyecto(proyecto: Proyecto) {
    if (!window.confirm(`¿Eliminar «${proyecto.titulo}»? Esta acción no se puede deshacer.`)) return;
    const response = await fetch(`/api/proyectos/${proyecto.idProyecto}`, { method: "DELETE" });
    if (!response.ok) {
      setError(response.status === 404 ? "No se encontró el proyecto dentro de tu empresa." : response.status === 409 ? "No se puede eliminar porque ya tiene postulaciones. Puedes cerrar la vacante." : "No se pudo eliminar el proyecto.");
      return;
    }
    setAviso("Proyecto eliminado.");
    await cargarProyectos();
  }

  const disponibles = proyectos.filter((proyecto) => proyecto.estado === "DISPONIBLE").length;

  return (
    <main className="min-h-screen bg-canvas px-5 py-7 text-ink md:px-10 md:py-10">
      {aviso && <Toast message={aviso} onDismiss={() => setAviso("")} />}
      <div className="mx-auto max-w-7xl">
        <header className="flex flex-wrap items-center justify-between gap-4"><a href="/" className="flex items-center gap-3"><span className="grid h-10 w-10 place-items-center rounded-xl bg-brand text-lg font-bold text-white">N</span><span><span className="block text-lg font-bold">nexo</span><span className="block text-xs text-slate-500">Espacio de empresa</span></span></a><button onClick={async () => { await fetch("/api/auth/logout", { method: "POST" }); router.replace("/login"); }} className="rounded-lg px-3 py-2 text-sm font-semibold text-slate-500 hover:bg-white">Cerrar sesión</button></header>

        <div className="mt-10 flex flex-col justify-between gap-5 md:flex-row md:items-end"><div><p className="text-xs font-semibold uppercase tracking-[.16em] text-brand">Gestión de talento</p><h1 className="mt-2 text-3xl font-bold tracking-tight">Tus proyectos</h1><p className="mt-2 text-sm text-slate-500">Publica oportunidades de residencia y administra sus vacantes.</p></div><div className="flex gap-3"><div className="rounded-xl border border-slate-200 bg-white px-5 py-3"><p className="text-xl font-bold">{cargando ? "—" : proyectos.length}</p><p className="text-xs text-slate-500">proyectos totales</p></div><div className="rounded-xl border border-slate-200 bg-white px-5 py-3"><p className="text-xl font-bold text-emerald-700">{cargando ? "—" : disponibles}</p><p className="text-xs text-slate-500">publicados</p></div></div></div>

        <div className="mt-8 grid items-start gap-6 xl:grid-cols-[minmax(0,1fr)_390px]">
          <section id="proyectos" className="rounded-2xl border border-slate-200 bg-white p-5 shadow-card md:p-6"><div className="flex items-center justify-between"><div><h2 className="text-lg font-bold">Proyectos de tu empresa</h2><p className="mt-1 text-sm text-slate-500">Solo tú puedes modificar estas publicaciones.</p></div><a href="#proyectos" className="text-xs font-semibold text-brand">Ir a proyectos ↓</a></div>
            {cargando ? <div className="mt-6 h-52 animate-pulse rounded-xl bg-slate-100"/> : proyectos.length === 0 ? <div className="mt-6 rounded-xl border border-dashed border-slate-300 px-5 py-14 text-center"><span className="mx-auto grid h-11 w-11 place-items-center rounded-xl bg-blue-50 text-lg text-brand">＋</span><p className="mt-4 text-sm font-semibold">Aún no publicas proyectos</p><p className="mt-1 text-xs text-slate-500">Completa el formulario para crear tu primera oportunidad.</p></div> : <div className="mt-5 space-y-3">{proyectos.map((proyecto) => <article key={proyecto.idProyecto} className="rounded-xl border border-slate-200 p-4"><div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-start"><div className="min-w-0"><div className="flex flex-wrap items-center gap-2"><span className="text-[11px] font-semibold uppercase tracking-wide text-brand">{proyecto.codigoProyecto}</span><span className={`rounded-full px-2.5 py-1 text-[10px] font-semibold ${proyecto.estado === "DISPONIBLE" ? "bg-emerald-50 text-emerald-700" : "bg-slate-100 text-slate-600"}`}>{proyecto.estado === "DISPONIBLE" ? "Publicado" : proyecto.estado === "CERRADO" ? "Cerrado" : "Asignado"}</span></div><h3 className="mt-2 font-bold">{proyecto.titulo}</h3><p className="mt-1 line-clamp-2 text-sm leading-5 text-slate-500">{proyecto.descripcion}</p><p className="mt-2 text-xs text-slate-400">Cupo total: {proyecto.cupoAlumnos} · {proyecto.tecnologiasRequeridas}</p></div><div className="flex shrink-0 gap-2"><button onClick={() => cambiarEstado(proyecto)} disabled={proyecto.estado === "ASIGNADO"} className="rounded-lg border border-slate-200 px-3 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-50 disabled:opacity-50">{proyecto.estado === "DISPONIBLE" ? "Cerrar" : proyecto.estado === "ASIGNADO" ? "Cupo completo" : "Reabrir"}</button><button onClick={() => eliminarProyecto(proyecto)} className="rounded-lg px-3 py-2 text-xs font-semibold text-rose-600 hover:bg-rose-50">Eliminar</button></div></div></article>)}</div>}
          </section>

          <aside className="rounded-2xl border border-slate-200 bg-white p-5 shadow-card md:p-6"><h2 className="text-lg font-bold">Publicar proyecto</h2><p className="mt-1 text-sm leading-5 text-slate-500">Los proyectos se publican con estado disponible. La coordinación puede revisar el catálogo institucional.</p><form noValidate onSubmit={crearProyecto} className="mt-5 space-y-3.5">
            <label className="block"><span className="mb-1.5 block text-xs font-semibold text-slate-700">Código de proyecto</span><input required maxLength={40} value={formulario.codigoProyecto} onChange={(event) => actualizarCampo("codigoProyecto", event.target.value)} onBlur={() => setTocados((actual) => ({ ...actual, codigoProyecto: true }))} aria-invalid={Boolean(tocados.codigoProyecto && erroresCampos.codigoProyecto)} placeholder="Ej. RES-2026-01" className={`h-10 w-full rounded-lg border px-3 text-sm outline-none focus:ring-4 focus:ring-blue-100 ${tocados.codigoProyecto && erroresCampos.codigoProyecto ? "border-rose-400 focus:border-rose-500" : "border-slate-200 focus:border-brand"}`}/>{tocados.codigoProyecto && erroresCampos.codigoProyecto && <span className="mt-1 block text-[11px] text-rose-600">{erroresCampos.codigoProyecto}</span>}</label>
            <label className="block"><span className="mb-1.5 block text-xs font-semibold text-slate-700">Título</span><input required minLength={5} maxLength={160} value={formulario.titulo} onChange={(event) => actualizarCampo("titulo", event.target.value)} onBlur={() => setTocados((actual) => ({ ...actual, titulo: true }))} aria-invalid={Boolean(tocados.titulo && erroresCampos.titulo)} placeholder="Desarrollo de plataforma web" className={`h-10 w-full rounded-lg border px-3 text-sm outline-none focus:ring-4 focus:ring-blue-100 ${tocados.titulo && erroresCampos.titulo ? "border-rose-400 focus:border-rose-500" : "border-slate-200 focus:border-brand"}`}/>{tocados.titulo && erroresCampos.titulo && <span className="mt-1 block text-[11px] text-rose-600">{erroresCampos.titulo}</span>}</label>
            <label className="block"><span className="mb-1.5 block text-xs font-semibold text-slate-700">Descripción</span><textarea required minLength={20} maxLength={5000} rows={3} value={formulario.descripcion} onChange={(event) => actualizarCampo("descripcion", event.target.value)} onBlur={() => setTocados((actual) => ({ ...actual, descripcion: true }))} aria-invalid={Boolean(tocados.descripcion && erroresCampos.descripcion)} className={`w-full rounded-lg border px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-blue-100 ${tocados.descripcion && erroresCampos.descripcion ? "border-rose-400 focus:border-rose-500" : "border-slate-200 focus:border-brand"}`} placeholder="¿En qué trabajará el residente?"/>{tocados.descripcion && erroresCampos.descripcion && <span className="mt-1 block text-[11px] text-rose-600">{erroresCampos.descripcion}</span>}</label>
            <label className="block"><span className="mb-1.5 block text-xs font-semibold text-slate-700">Objetivo</span><textarea required minLength={10} maxLength={5000} rows={2} value={formulario.objetivo} onChange={(event) => actualizarCampo("objetivo", event.target.value)} onBlur={() => setTocados((actual) => ({ ...actual, objetivo: true }))} aria-invalid={Boolean(tocados.objetivo && erroresCampos.objetivo)} className={`w-full rounded-lg border px-3 py-2 text-sm outline-none focus:ring-4 focus:ring-blue-100 ${tocados.objetivo && erroresCampos.objetivo ? "border-rose-400 focus:border-rose-500" : "border-slate-200 focus:border-brand"}`} placeholder="Resultado esperado al terminar la residencia"/>{tocados.objetivo && erroresCampos.objetivo && <span className="mt-1 block text-[11px] text-rose-600">{erroresCampos.objetivo}</span>}</label>
            <label className="block"><span className="mb-1.5 block text-xs font-semibold text-slate-700">Tecnologías o competencias</span><input required minLength={2} maxLength={1000} value={formulario.tecnologiasRequeridas} onChange={(event) => actualizarCampo("tecnologiasRequeridas", event.target.value)} onBlur={() => setTocados((actual) => ({ ...actual, tecnologiasRequeridas: true }))} aria-invalid={Boolean(tocados.tecnologiasRequeridas && erroresCampos.tecnologiasRequeridas)} placeholder="Java, PostgreSQL, análisis de datos" className={`h-10 w-full rounded-lg border px-3 text-sm outline-none focus:ring-4 focus:ring-blue-100 ${tocados.tecnologiasRequeridas && erroresCampos.tecnologiasRequeridas ? "border-rose-400 focus:border-rose-500" : "border-slate-200 focus:border-brand"}`}/>{tocados.tecnologiasRequeridas && erroresCampos.tecnologiasRequeridas && <span className="mt-1 block text-[11px] text-rose-600">{erroresCampos.tecnologiasRequeridas}</span>}<span className="mt-1 block text-[11px] text-slate-400">Separa cada tecnología con coma.</span></label>
            <label className="block"><span className="mb-1.5 block text-xs font-semibold text-slate-700">Cupo de residentes</span><input required min="1" max="100" step="1" type="number" value={formulario.cupoAlumnos} onChange={(event) => actualizarCampo("cupoAlumnos", event.target.value)} onBlur={() => setTocados((actual) => ({ ...actual, cupoAlumnos: true }))} aria-invalid={Boolean(tocados.cupoAlumnos && erroresCampos.cupoAlumnos)} className={`h-10 w-full rounded-lg border px-3 text-sm outline-none focus:ring-4 focus:ring-blue-100 ${tocados.cupoAlumnos && erroresCampos.cupoAlumnos ? "border-rose-400 focus:border-rose-500" : "border-slate-200 focus:border-brand"}`}/>{tocados.cupoAlumnos && erroresCampos.cupoAlumnos && <span className="mt-1 block text-[11px] text-rose-600">{erroresCampos.cupoAlumnos}</span>}</label>
            {error && <p role="alert" className="rounded-lg bg-rose-50 p-3 text-xs leading-5 text-rose-700">{error}</p>}
            <button disabled={guardando} className="h-11 w-full rounded-lg bg-brand text-sm font-semibold text-white hover:bg-blue-700 disabled:opacity-60">{guardando ? "Publicando…" : "Publicar oportunidad →"}</button>
          </form></aside>
        </div>
        <footer className="mt-9 border-t border-slate-200 pt-5 text-xs text-slate-400">Nexo · Portal de residencias profesionales</footer>
      </div>
    </main>
  );
}
