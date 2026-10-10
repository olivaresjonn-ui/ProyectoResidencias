"use client";

import { useEffect, useMemo, useState } from "react";
import { ApiError, obtenerProyectos, obtenerSesion, type Proyecto, type SesionUsuario } from "../lib/api";
import Recomendaciones from "./recomendaciones";
import { Toast } from "../components/toast";

const navItems = [
  { icon: "⌂", label: "Inicio", active: false },
  { icon: "▦", label: "Explorar proyectos", active: true },
  { icon: "♙", label: "Mi perfil", active: false },
  { icon: "◇", label: "Mis postulaciones", active: false },
  { icon: "▤", label: "Mi expediente", active: false },
];

const navPorRol: Record<SesionUsuario["rol"], typeof navItems> = {
  ALUMNO: navItems,
  EMPRESA: [
    { icon: "⌂", label: "Panel de empresa", active: true },
    { icon: "▦", label: "Mis proyectos", active: false },
    { icon: "◇", label: "Postulaciones recibidas", active: false },
    { icon: "▤", label: "Perfil de empresa", active: false },
  ],
  ASESOR: [
    { icon: "⌂", label: "Panel de asesoría", active: true },
    { icon: "♙", label: "Alumnos asignados", active: false },
    { icon: "▤", label: "Expedientes", active: false },
    { icon: "▦", label: "Proyectos", active: false },
  ],
  ADMIN: [
    { icon: "⌂", label: "Panel institucional", active: true },
    { icon: "♙", label: "Alumnos", active: false },
    { icon: "▦", label: "Proyectos", active: false },
    { icon: "▤", label: "Empresas y asesores", active: false },
  ],
};

function etiquetasDe(texto: string) {
  return texto.split(/[,;|]/).map((item) => item.trim()).filter(Boolean).slice(0, 4);
}

const nombresRol: Record<SesionUsuario["rol"], string> = {
  ADMIN: "Administrador",
  ALUMNO: "Alumno",
  EMPRESA: "Empresa",
  ASESOR: "Asesor académico",
};

export default function OportunidadesPage() {
  const [proyectos, setProyectos] = useState<Proyecto[]>([]);
  const [sesion, setSesion] = useState<SesionUsuario | null>(null);
  const [busqueda, setBusqueda] = useState("");
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");
  const [toast, setToast] = useState<{ mensaje: string; error?: boolean } | null>(null);
  const [postulandoId, setPostulandoId] = useState<number | null>(null);

  async function postular(idProyecto: number) {
    setPostulandoId(idProyecto);
    setToast(null);
    try {
      const response = await fetch("/api/postulaciones", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ idProyecto }),
      });
      if (response.status === 401) {
        window.location.assign("/login");
        return;
      }
      if (!response.ok) {
        setToast({ mensaje: response.status === 409 ? "Ya te postulaste a este proyecto o sus cupos se completaron." : "No se pudo enviar la postulación. Intenta de nuevo.", error: true });
        return;
      }
      const resultado = await response.json() as { folioPostulacion: string };
      setToast({ mensaje: `Postulación enviada. Tu folio es ${resultado.folioPostulacion}.` });
    } catch {
      setToast({ mensaje: "No se pudo contactar al backend. Intenta de nuevo en unos momentos.", error: true });
    } finally {
      setPostulandoId(null);
    }
  }

  useEffect(() => {
    Promise.all([obtenerProyectos(), obtenerSesion()])
      .then(([datosProyectos, datosSesion]) => {
        if (datosSesion.rol === "EMPRESA") {
          window.location.replace("/empresa/proyectos");
          return;
        }
        if (datosSesion.rol === "ASESOR") {
          window.location.replace("/asesor");
          return;
        }
        if (datosSesion.rol === "ADMIN") {
          window.location.replace("/admin/asignaciones");
          return;
        }
        setProyectos(datosProyectos);
        setSesion(datosSesion);
      })
      .catch((cause: unknown) => {
        if (cause instanceof ApiError && cause.status === 401) {
          window.location.assign("/login");
          return;
        }
        setError(cause instanceof Error ? cause.message : "No se pudieron cargar los proyectos.");
      })
      .finally(() => setCargando(false));
  }, []);

  const proyectosVisibles = useMemo(() => {
    const termino = busqueda.trim().toLocaleLowerCase();
    return proyectos.filter((proyecto) => {
      const texto = `${proyecto.titulo} ${proyecto.descripcion} ${proyecto.tecnologiasRequeridas} ${proyecto.empresa?.razonSocial ?? ""}`.toLocaleLowerCase();
      return proyecto.estado === "DISPONIBLE" && (!termino || texto.includes(termino));
    });
  }, [proyectos, busqueda]);
  const menuActual = sesion ? navPorRol[sesion.rol] : navItems;
  const nombreEspacio = sesion ? nombresRol[sesion.rol] : "usuario";

  return (
    <main className="min-h-screen bg-canvas text-ink">
      <aside className="fixed inset-y-0 left-0 z-20 hidden w-[254px] flex-col border-r border-slate-200/80 bg-white px-5 py-7 lg:flex">
        <a href="#contenido" className="mb-12 flex items-center gap-3 px-2">
          <span className="grid h-10 w-10 place-items-center rounded-xl bg-brand text-lg font-bold text-white">N</span>
          <span><span className="block text-lg font-bold tracking-tight">nexo</span><span className="text-xs text-slate-500">Residencias profesionales</span></span>
        </a>
        <p className="mb-3 px-3 text-[10px] font-bold uppercase tracking-[0.16em] text-slate-400">Espacio de {nombreEspacio.toLocaleLowerCase()}</p>
        <nav className="space-y-1">
          {menuActual.map((item) => <a key={item.label} href={item.label === "Mis postulaciones" ? "/postulaciones" : item.label === "Mi expediente" ? "/expediente" : item.label === "Mi perfil" ? "/perfil" : item.label === "Mis proyectos" ? "/empresa/proyectos" : "#contenido"} className={`flex items-center gap-3 rounded-xl px-3 py-3 text-sm font-medium transition ${item.active ? "bg-blue-50 text-brand" : "text-slate-500 hover:bg-slate-50 hover:text-ink"}`}><span className="w-5 text-center text-lg">{item.icon}</span>{item.label}</a>)}
        </nav>
        <div className="mt-auto rounded-2xl bg-[#f5f7fb] p-4">
          <div className="mb-3 flex h-8 w-8 items-center justify-center rounded-lg bg-white text-brand shadow-sm">?</div>
          <p className="text-sm font-semibold">¿Necesitas ayuda?</p>
          <p className="mt-1 text-xs leading-5 text-slate-500">Consulta a tu coordinación de residencias.</p>
          <a href="mailto:coordinacion@institucion.edu" className="mt-3 inline-block text-xs font-semibold text-brand">Contactar coordinación →</a>
        </div>
      </aside>

      <section id="contenido" className="lg:pl-[254px]">
        <header className="sticky top-0 z-10 flex h-[72px] items-center justify-between border-b border-slate-200/80 bg-white/90 px-5 backdrop-blur md:px-10">
          <div className="flex items-center gap-3"><span className="grid h-9 w-9 place-items-center rounded-xl bg-brand text-sm font-bold text-white lg:hidden">N</span><div><p className="text-xs text-slate-400">Portal · {nombreEspacio}</p><p className="text-sm font-semibold">Ciclo de residencias 2026</p></div></div>
          <div className="flex items-center gap-3"><button aria-label="Notificaciones" className="relative grid h-10 w-10 place-items-center rounded-xl text-lg text-slate-500 hover:bg-slate-100">♧<span className="absolute right-2 top-2 h-2 w-2 rounded-full bg-orange-400 ring-2 ring-white" /></button><div className="hidden h-8 w-px bg-slate-200 sm:block"/><button onClick={async () => { await fetch("/api/auth/logout", { method: "POST" }); window.location.assign("/login"); }} className="flex items-center gap-2 rounded-xl p-1.5 pr-2 hover:bg-slate-50"><span className="grid h-9 w-9 place-items-center rounded-full bg-[#e8edff] text-sm font-bold text-brand">{sesion?.correo.slice(0, 2).toUpperCase() ?? "…"}</span><span className="hidden text-left sm:block"><span className="block text-xs font-semibold">{sesion ? nombresRol[sesion.rol] : "Cargando perfil"}</span><span className="block text-[11px] text-slate-400">{sesion?.correo ?? ""}</span></span><span className="hidden text-xs text-slate-400 sm:block">Salir</span></button></div>
        </header>
        {sesion?.rol === "ALUMNO" && <nav aria-label="Navegación del alumno" className="flex gap-2 overflow-x-auto border-b border-slate-200 bg-white px-4 py-2 lg:hidden">
          <a href="/" aria-current="page" className="shrink-0 rounded-lg bg-blue-50 px-3 py-2 text-xs font-semibold text-brand">Explorar</a>
          <a href="/perfil" className="shrink-0 rounded-lg px-3 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-50">Mi perfil profesional</a>
          <a href="/postulaciones" className="shrink-0 rounded-lg px-3 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-50">Mis postulaciones</a>
          <a href="/expediente" className="shrink-0 rounded-lg px-3 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-50">Mi expediente</a>
        </nav>}

        <div className="mx-auto max-w-[1440px] px-5 py-8 md:px-10 md:py-10">
          <div className="mb-8 flex flex-col justify-between gap-5 md:flex-row md:items-end">
            <div><div className="mb-3 flex items-center gap-2 text-xs font-medium text-slate-400"><span>Inicio</span><span>/</span><span className="text-slate-600">Explorar proyectos</span></div><p className="text-sm text-slate-500">Convocatoria de residencias · 2026</p><h1 className="mt-1 text-3xl font-bold tracking-tight md:text-[34px]">Encuentra tu próximo reto<span className="text-brand">.</span></h1><p className="mt-2 max-w-2xl text-sm leading-6 text-slate-500">Explora proyectos de residencia y encuentra uno que conecte con lo que sabes y lo que quieres aprender.</p></div>
            <a href="#lista-proyectos" className="inline-flex h-11 items-center justify-center gap-2 rounded-xl bg-brand px-5 text-sm font-semibold text-white shadow-lg shadow-blue-900/15 transition hover:-translate-y-0.5 hover:bg-blue-700">Explorar vacantes <span>↗</span></a>
          </div>

          <section className="relative mb-8 overflow-hidden rounded-[24px] bg-[#172b4d] px-6 py-7 text-white md:px-9 md:py-9">
            <div className="absolute -right-16 -top-32 h-80 w-80 rounded-full border-[1px] border-white/10"/><div className="absolute -right-2 -top-24 h-64 w-64 rounded-full border-[1px] border-white/10"/>
            <div className="relative max-w-2xl"><span className="inline-flex items-center gap-2 rounded-full bg-white/10 px-3 py-1.5 text-[11px] font-medium text-blue-100"><span className="h-1.5 w-1.5 rounded-full bg-emerald-300"/> TU SIGUIENTE ETAPA EMPIEZA AQUÍ</span><h2 className="mt-5 text-2xl font-semibold leading-tight md:text-[28px]">Convierte lo que has aprendido<br className="hidden sm:block"/> en experiencia real.</h2><p className="mt-3 max-w-lg text-sm leading-6 text-blue-100/75">Revisa oportunidades, conoce a las empresas y postúlate al proyecto que va contigo.</p><div className="mt-6 flex flex-wrap gap-6"><div><p className="text-2xl font-bold">{cargando ? "—" : proyectosVisibles.length}</p><p className="mt-1 text-xs text-blue-100/70">proyectos disponibles</p></div><div className="h-10 w-px bg-white/15"/><div><p className="text-2xl font-bold">A tu medida</p><p className="mt-1 text-xs text-blue-100/70">filtra por tecnología o empresa</p></div></div></div>
          </section>

          {sesion?.rol === "ALUMNO" && <Recomendaciones />}
          <div id="lista-proyectos" className="mb-5 flex flex-col justify-between gap-4 sm:flex-row sm:items-center"><div><h2 className="text-xl font-bold tracking-tight">Proyectos disponibles</h2><p className="mt-1 text-sm text-slate-500">Oportunidades abiertas para tu residencia profesional</p></div><label className="relative block w-full sm:w-[310px]"><span className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400">⌕</span><input value={busqueda} onChange={(event) => setBusqueda(event.target.value)} placeholder="Buscar por título, empresa o tecnología" className="h-11 w-full rounded-xl border border-slate-200 bg-white pl-10 pr-4 text-sm outline-none transition placeholder:text-slate-400 focus:border-brand focus:ring-4 focus:ring-blue-100"/></label></div>

          {error ? <div className="rounded-2xl border border-amber-200 bg-amber-50 p-5 text-sm text-amber-950"><p className="font-semibold">No pudimos conectar con el backend</p><p className="mt-1 text-amber-900/80">{error} Confirma que Spring Boot esté iniciado y que <code className="rounded bg-white/70 px-1">BACKEND_BASE_URL</code> apunte a la dirección correcta.</p></div> : cargando ? <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">{[1, 2, 3].map((item) => <div key={item} className="h-64 animate-pulse rounded-2xl border border-slate-100 bg-white shadow-card"><div className="m-5 h-5 w-2/3 rounded bg-slate-100"/><div className="mx-5 mt-4 h-3 rounded bg-slate-100"/><div className="mx-5 mt-2 h-3 w-4/5 rounded bg-slate-100"/></div>)}</div> : proyectosVisibles.length === 0 ? <div className="rounded-2xl border border-dashed border-slate-300 bg-white px-6 py-14 text-center"><span className="mx-auto grid h-12 w-12 place-items-center rounded-2xl bg-blue-50 text-xl text-brand">⌕</span><h3 className="mt-4 font-semibold">{busqueda ? "No encontramos coincidencias" : "Aún no hay proyectos disponibles"}</h3><p className="mx-auto mt-2 max-w-md text-sm leading-6 text-slate-500">{busqueda ? "Prueba otra búsqueda con una tecnología, empresa o palabra distinta." : "Cuando las empresas publiquen vacantes, aparecerán aquí. Mientras tanto puedes volver más tarde."}</p></div> : <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">{proyectosVisibles.map((proyecto) => <article key={proyecto.idProyecto} className="group flex min-h-[278px] flex-col rounded-2xl border border-slate-200/80 bg-white p-5 shadow-card transition hover:-translate-y-1 hover:border-blue-200 hover:shadow-lg"><div className="flex items-start justify-between gap-3"><span className="grid h-11 w-11 place-items-center rounded-xl bg-[#eef2ff] text-lg font-bold text-brand">{(proyecto.empresa?.razonSocial ?? "P").slice(0, 1).toUpperCase()}</span><span className="rounded-full bg-emerald-50 px-2.5 py-1 text-[10px] font-semibold uppercase tracking-wide text-emerald-700">Disponible</span></div><p className="mt-4 text-xs font-medium text-brand">{proyecto.empresa?.razonSocial ?? "Empresa participante"}</p><h3 className="mt-1 text-base font-bold leading-snug">{proyecto.titulo}</h3><p className="mt-2 line-clamp-2 text-sm leading-5 text-slate-500">{proyecto.descripcion}</p><div className="mt-4 flex flex-wrap gap-1.5">{etiquetasDe(proyecto.tecnologiasRequeridas).map((tag) => <span key={tag} className="rounded-md bg-slate-100 px-2 py-1 text-[10px] font-medium text-slate-600">{tag}</span>)}</div><div className="mt-auto flex items-center justify-between border-t border-slate-100 pt-4"><span className="text-xs text-slate-500">Cupo total: {proyecto.cupoAlumnos}</span>{sesion?.rol === "ALUMNO" && <button onClick={() => postular(proyecto.idProyecto)} disabled={postulandoId === proyecto.idProyecto} className="rounded-lg bg-blue-50 px-3 py-2 text-xs font-semibold text-brand transition hover:bg-blue-100 disabled:opacity-60">{postulandoId === proyecto.idProyecto ? "Enviando…" : "Postularme →"}</button>}</div></article>)}</div>}
          {toast && <Toast message={toast.mensaje} variant={toast.error ? "error" : "success"} onDismiss={() => setToast(null)} />}
          <footer className="mt-10 flex flex-col justify-between gap-2 border-t border-slate-200 pt-5 text-xs text-slate-400 sm:flex-row"><span>© 2026 Nexo · Portal de residencias profesionales</span><span>¿Problemas? Contacta a tu coordinación académica</span></footer>
        </div>
      </section>
    </main>
  );
}
