"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { Toast } from "../../components/toast";

type Alumno = { numeroControl: string; nombreCompleto: string; carrera: string; semestre: number; estadoResidencia: string };
type Postulacion = { idPostulacion: number; nombreAlumno: string; carreraAlumno: string; tituloProyecto: string; empresa: string; estadoPostulacion: string; fechaSolicitud: string; comentarios?: string };
type Documento = { idDocumento: number; idPostulacion: number; tipoDocumento: string; fechaSubida: string; estadoAprobacion: string };

export default function PanelAsesor() {
  const router = useRouter();
  const [alumnos, setAlumnos] = useState<Alumno[]>([]);
  const [postulaciones, setPostulaciones] = useState<Postulacion[]>([]);
  const [documentos, setDocumentos] = useState<Documento[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");
  const [aviso, setAviso] = useState("");

  async function cargar() {
    const respuestas = await Promise.all([fetch("/api/auth/me"), fetch("/api/alumnos"), fetch("/api/postulaciones"), fetch("/api/documentos")]);
    if (respuestas.some((r) => r.status === 401)) { router.replace("/login"); return; }
    const sesion = await respuestas[0].json() as { rol?: string };
    if (sesion.rol !== "ASESOR") { router.replace("/"); return; }
    const fallida = respuestas.find((r) => !r.ok);
    if (fallida) throw new Error("No se pudieron cargar tus alumnos, postulaciones o documentos.");
    const [a, p, d] = await Promise.all(respuestas.slice(1).map((r) => r.json()));
    setAlumnos(a as Alumno[]); setPostulaciones(p as Postulacion[]); setDocumentos(d as Documento[]);
  }

  useEffect(() => { cargar().catch((e: unknown) => setError(e instanceof Error ? e.message : "Error de conexión.")).finally(() => setCargando(false)); }, []); // eslint-disable-line react-hooks/exhaustive-deps

  async function decidirPostulacion(item: Postulacion, estadoPostulacion: "ACEPTADA" | "RECHAZADA") {
    setError(""); setAviso("");
    const response = await fetch(`/api/postulaciones/${item.idPostulacion}`, { method: "PUT", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ estadoPostulacion, comentarios: item.comentarios ?? "Revisada por asesor académico." }) });
    if (!response.ok) { setError(response.status === 409 ? "No hay cupos disponibles en el proyecto para aceptar a este alumno." : "No se pudo actualizar la postulación. Confirma que siga asignada a tu asesoría."); return; }
    setAviso(`Postulación de ${item.nombreAlumno} marcada como ${estadoPostulacion.toLowerCase()}.`); await cargar();
  }

  async function decidirDocumento(documento: Documento, estadoAprobacion: "APROBADO" | "RECHAZADO") {
    setError(""); setAviso("");
    const response = await fetch(`/api/documentos/${documento.idDocumento}/estado`, { method: "PATCH", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ estadoAprobacion }) });
    if (!response.ok) { setError("No se pudo revisar el documento."); return; }
    setAviso("El estado del documento se actualizó."); await cargar();
  }

  const pendientes = postulaciones.filter((p) => p.estadoPostulacion === "EN_REVISION").length;
  const docsPendientes = documentos.filter((d) => d.estadoAprobacion === "PENDIENTE").length;
  return <main className="min-h-screen bg-canvas px-5 py-8 text-ink md:px-10"><div className="mx-auto max-w-7xl">
    <header className="flex items-center justify-between"><a href="/" className="flex items-center gap-3"><span className="grid h-10 w-10 place-items-center rounded-xl bg-brand font-bold text-white">N</span><span><b className="block">nexo</b><small className="text-slate-500">Espacio de asesoría</small></span></a><button onClick={async () => { await fetch("/api/auth/logout", { method: "POST" }); router.replace("/login"); }} className="text-sm font-semibold text-slate-500">Cerrar sesión</button></header>
    <div className="mt-10"><p className="text-xs font-bold uppercase tracking-widest text-brand">Coordinación académica</p><h1 className="mt-2 text-3xl font-bold">Panel de asesoría</h1><p className="mt-2 text-sm text-slate-500">Da seguimiento a los alumnos asignados, revisa sus postulaciones y valida sus documentos.</p></div>
    <div className="mt-6 grid gap-4 sm:grid-cols-3">{[["Alumnos asignados", alumnos.length], ["Postulaciones por revisar", pendientes], ["Documentos pendientes", docsPendientes]].map(([label, n]) => <article key={String(label)} className="rounded-2xl border border-slate-200 bg-white p-5"><p className="text-sm text-slate-500">{label}</p><p className="mt-2 text-3xl font-bold">{cargando ? "—" : n}</p></article>)}</div>
    {error && <p role="alert" className="mt-5 rounded-xl bg-rose-50 p-4 text-sm text-rose-700">{error}</p>}{aviso && <Toast message={aviso} onDismiss={() => setAviso("")} />}
    <section className="mt-8 grid items-start gap-6 xl:grid-cols-2"><article className="rounded-2xl border border-slate-200 bg-white p-5"><h2 className="text-lg font-bold">Alumnos asignados</h2>{cargando ? <div className="mt-4 h-32 animate-pulse rounded-xl bg-slate-100"/> : alumnos.length ? <div className="mt-4 divide-y divide-slate-100">{alumnos.map((a) => <div key={a.numeroControl} className="py-3"><p className="font-semibold">{a.nombreCompleto}</p><p className="mt-1 text-xs text-slate-500">{a.numeroControl} · {a.carrera} · semestre {a.semestre}</p></div>)}</div> : <p className="mt-4 text-sm text-slate-500">Aún no tienes alumnos asignados. Solicita a administración que vincule sus expedientes.</p>}</article>
    <article className="rounded-2xl border border-slate-200 bg-white p-5"><h2 className="text-lg font-bold">Postulaciones</h2>{!cargando && postulaciones.length === 0 && <p className="mt-4 text-sm text-slate-500">No hay postulaciones de tus alumnos.</p>}<div className="mt-3 space-y-3">{postulaciones.map((p) => <div key={p.idPostulacion} className="rounded-xl border border-slate-100 p-4"><div className="flex justify-between gap-3"><div><p className="font-semibold">{p.nombreAlumno}</p><p className="mt-1 text-sm">{p.tituloProyecto}</p><p className="text-xs text-slate-500">{p.carreraAlumno} · {p.empresa} · {p.fechaSolicitud}</p></div><span className="h-fit rounded-full bg-blue-50 px-2.5 py-1 text-[10px] font-semibold text-blue-700">{p.estadoPostulacion.replaceAll("_", " ")}</span></div>{p.estadoPostulacion === "EN_REVISION" && <div className="mt-3 flex gap-2"><button onClick={() => decidirPostulacion(p, "ACEPTADA")} className="rounded-lg bg-emerald-600 px-3 py-2 text-xs font-semibold text-white">Aceptar</button><button onClick={() => decidirPostulacion(p, "RECHAZADA")} className="rounded-lg bg-rose-50 px-3 py-2 text-xs font-semibold text-rose-700">Rechazar</button></div>}</div>)}</div></article></section>
    <section className="mt-6 rounded-2xl border border-slate-200 bg-white p-5"><h2 className="text-lg font-bold">Documentos de los expedientes</h2>{!cargando && documentos.length === 0 && <p className="mt-3 text-sm text-slate-500">No hay documentos cargados por tus alumnos.</p>}<div className="mt-3 grid gap-3 md:grid-cols-2">{documentos.map((d) => <div key={d.idDocumento} className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-slate-100 p-4"><div><p className="font-semibold">{d.tipoDocumento.replaceAll("_", " ")}</p><p className="text-xs text-slate-500">Postulación #{d.idPostulacion} · {d.fechaSubida} · {d.estadoAprobacion}</p></div><div className="flex gap-2"><a href={`/api/documentos/${d.idDocumento}/archivo`} className="rounded-lg border px-3 py-2 text-xs font-semibold">Descargar</a>{d.estadoAprobacion === "PENDIENTE" && <><button onClick={() => decidirDocumento(d, "APROBADO")} className="rounded-lg bg-emerald-600 px-3 py-2 text-xs font-semibold text-white">Aprobar</button><button onClick={() => decidirDocumento(d, "RECHAZADO")} className="rounded-lg bg-rose-50 px-3 py-2 text-xs font-semibold text-rose-700">Rechazar</button></>}</div></div>)}</div></section>
  </div></main>;
}
