"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { ApiError } from "../../lib/api";

type Postulacion = {
  idPostulacion: number;
  folioPostulacion: string;
  idProyecto: number;
  tituloProyecto: string;
  empresa: string;
  fechaSolicitud: string;
  estadoPostulacion: "EN_REVISION" | "ACEPTADA" | "RECHAZADA";
  comentarios: string | null;
};

const estadoTexto = {
  EN_REVISION: "En revisión",
  ACEPTADA: "Aceptada",
  RECHAZADA: "No seleccionada",
};

export default function MisPostulacionesPage() {
  const router = useRouter();
  const [postulaciones, setPostulaciones] = useState<Postulacion[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    fetch("/api/postulaciones/mias", { cache: "no-store" })
      .then(async (response) => {
        if (response.status === 401) throw new ApiError("La sesión terminó.", 401);
        if (!response.ok) throw new Error("No se pudieron consultar tus postulaciones.");
        return response.json() as Promise<Postulacion[]>;
      })
      .then(setPostulaciones)
      .catch((cause: unknown) => {
        if (cause instanceof ApiError && cause.status === 401) {
          router.replace("/login");
          return;
        }
        setError(cause instanceof Error ? cause.message : "Ocurrió un error inesperado.");
      })
      .finally(() => setCargando(false));
  }, [router]);

  return (
    <main className="min-h-screen bg-canvas px-5 py-8 text-ink md:px-10 md:py-12">
      <div className="mx-auto max-w-4xl">
        <a href="/" className="text-sm font-semibold text-brand">← Volver a proyectos</a>
        <p className="mt-8 text-xs font-semibold uppercase tracking-[.16em] text-brand">Mi proceso</p>
        <h1 className="mt-2 text-3xl font-bold tracking-tight">Mis postulaciones</h1>
        <p className="mt-2 text-sm text-slate-500">Consulta el avance de las oportunidades a las que te postulaste.</p>
        {cargando ? <div className="mt-8 h-48 animate-pulse rounded-2xl bg-white shadow-card"/> : error ? <div role="alert" className="mt-8 rounded-xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-900">{error}</div> : postulaciones.length === 0 ? <div className="mt-8 rounded-2xl border border-dashed border-slate-300 bg-white px-6 py-14 text-center"><p className="font-semibold">Todavía no tienes postulaciones</p><p className="mt-2 text-sm text-slate-500">Cuando encuentres un proyecto, puedes enviar tu solicitud desde su tarjeta.</p><a href="/" className="mt-5 inline-flex rounded-xl bg-brand px-4 py-2.5 text-sm font-semibold text-white">Explorar proyectos</a></div> : <div className="mt-8 space-y-3">{postulaciones.map((postulacion) => <article key={postulacion.idPostulacion} className="rounded-2xl border border-slate-200 bg-white p-5 shadow-card sm:flex sm:items-center sm:justify-between"><div><p className="text-xs font-medium text-brand">{postulacion.empresa} · {postulacion.folioPostulacion}</p><h2 className="mt-1 font-bold">{postulacion.tituloProyecto}</h2><p className="mt-1 text-xs text-slate-500">Enviada el {new Date(`${postulacion.fechaSolicitud}T12:00:00`).toLocaleDateString("es-MX", { dateStyle: "long" })}</p>{postulacion.comentarios && <p className="mt-3 rounded-lg bg-slate-50 p-3 text-sm text-slate-600">{postulacion.comentarios}</p>}</div><span className={`mt-4 inline-flex rounded-full px-3 py-1.5 text-xs font-semibold sm:mt-0 ${postulacion.estadoPostulacion === "ACEPTADA" ? "bg-emerald-50 text-emerald-700" : postulacion.estadoPostulacion === "RECHAZADA" ? "bg-rose-50 text-rose-700" : "bg-amber-50 text-amber-700"}`}>{estadoTexto[postulacion.estadoPostulacion]}</span></article>)}</div>}
      </div>
    </main>
  );
}
