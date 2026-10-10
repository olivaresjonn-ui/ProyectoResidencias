"use client";

import { FormEvent, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { Toast } from "../../components/toast";

type Postulacion = { idPostulacion: number; folioPostulacion: string; tituloProyecto: string };
type Documento = {
  idDocumento: number;
  folioDocumento: string;
  idPostulacion: number;
  tipoDocumento: "ANTEPROYECTO" | "REPORTE_BIMESTRAL" | "REPORTE_FINAL";
  fechaSubida: string;
  estadoAprobacion: "PENDIENTE" | "APROBADO" | "RECHAZADO";
};

const tipoDocumentoTexto: Record<Documento["tipoDocumento"], string> = {
  ANTEPROYECTO: "Anteproyecto",
  REPORTE_BIMESTRAL: "Reporte bimestral",
  REPORTE_FINAL: "Reporte final",
};
const estadoDocumentoTexto: Record<Documento["estadoAprobacion"], string> = {
  PENDIENTE: "Pendiente de revisión",
  APROBADO: "Aprobado",
  RECHAZADO: "Requiere correcciones",
};

export default function ExpedientePage() {
  const router = useRouter();
  const [postulaciones, setPostulaciones] = useState<Postulacion[]>([]);
  const [documentos, setDocumentos] = useState<Documento[]>([]);
  const [postulacionId, setPostulacionId] = useState("");
  const [tipoDocumento, setTipoDocumento] = useState<Documento["tipoDocumento"]>("ANTEPROYECTO");
  const [archivo, setArchivo] = useState<File | null>(null);
  const [cargando, setCargando] = useState(true);
  const [subiendo, setSubiendo] = useState(false);
  const [error, setError] = useState("");
  const [toast, setToast] = useState("");
  const errorArchivo = !archivo ? "" : archivo.type !== "application/pdf" || !archivo.name.toLowerCase().endsWith(".pdf")
    ? "El archivo debe ser un PDF válido."
    : archivo.size > 10 * 1024 * 1024 ? "El PDF no puede superar 10 MB." : "";

  async function cargarDatos() {
    const [resPostulaciones, resDocumentos] = await Promise.all([
      fetch("/api/postulaciones/mias", { cache: "no-store" }),
      fetch("/api/documentos", { cache: "no-store" }),
    ]);
    if (resPostulaciones.status === 401 || resDocumentos.status === 401) {
      router.replace("/login");
      return;
    }
    if (!resPostulaciones.ok || !resDocumentos.ok) throw new Error("No se pudo cargar el expediente.");
    const [apps, docs] = await Promise.all([resPostulaciones.json(), resDocumentos.json()]) as [Postulacion[], Documento[]];
    setPostulaciones(apps);
    setDocumentos(docs);
    setPostulacionId((current) => current || String(apps[0]?.idPostulacion ?? ""));
  }

  useEffect(() => {
    cargarDatos()
      .catch((cause: unknown) => setError(cause instanceof Error ? cause.message : "Ocurrió un error inesperado."))
      .finally(() => setCargando(false));
  }, [router]);

  async function subirDocumento(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setToast("");
    if (!postulacionId) return setError("Primero necesitas una postulación aceptada o en revisión.");
    if (!archivo) return setError("Selecciona un PDF para subir.");
    if (errorArchivo) return setError(errorArchivo);

    const formulario = new FormData();
    formulario.set("idPostulacion", postulacionId);
    formulario.set("tipoDocumento", tipoDocumento);
    formulario.set("archivo", archivo);
    setSubiendo(true);
    try {
      const response = await fetch("/api/documentos", { method: "POST", body: formulario });
      if (response.status === 401) return router.replace("/login");
      if (!response.ok) {
        setError(response.status === 403 ? "Esta postulación no pertenece a tu cuenta." : "No se pudo guardar el documento. Revisa el tipo y tamaño del archivo.");
        return;
      }
      setArchivo(null);
      const selector = document.getElementById("archivo") as HTMLInputElement | null;
      if (selector) selector.value = "";
      setToast("Documento subido. Quedó pendiente de revisión académica.");
      await cargarDatos();
    } catch {
      setError("No se pudo contactar al backend. Intenta de nuevo.");
    } finally {
      setSubiendo(false);
    }
  }

  const tituloPorPostulacion = new Map(postulaciones.map((postulacion) => [postulacion.idPostulacion, postulacion.tituloProyecto]));

  return (
    <main className="min-h-screen bg-canvas px-5 py-8 text-ink md:px-10 md:py-12">
      {toast && <Toast message={toast} onDismiss={() => setToast("")} />}
      <div className="mx-auto max-w-5xl">
        <a href="/" className="text-sm font-semibold text-brand">← Volver al portal</a>
        <div className="mt-8"><p className="text-xs font-semibold uppercase tracking-[.16em] text-brand">Seguimiento académico</p><h1 className="mt-2 text-3xl font-bold tracking-tight">Mi expediente</h1><p className="mt-2 max-w-2xl text-sm leading-6 text-slate-500">Sube tus entregables en PDF y revisa el estado de aprobación. Los archivos se guardan fuera de la carpeta pública y solo tu cuenta y el personal revisor pueden consultarlos.</p></div>

        <div className="mt-8 grid gap-6 lg:grid-cols-[.9fr_1.1fr]">
          <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-card">
            <h2 className="text-lg font-bold">Subir un entregable</h2>
            <p className="mt-1 text-sm text-slate-500">Formatos PDF de hasta 10 MB.</p>
            {cargando ? <div className="mt-6 h-48 animate-pulse rounded-xl bg-slate-100"/> : postulaciones.length === 0 ? <div className="mt-6 rounded-xl bg-blue-50 p-4 text-sm leading-6 text-blue-900">Primero envía una postulación a un proyecto. Después podrás asociar tus documentos a ese proceso. <a className="font-semibold underline" href="/">Explorar proyectos</a></div> : <form onSubmit={subirDocumento} className="mt-6 space-y-4">
              <label className="block"><span className="mb-2 block text-sm font-semibold">Postulación</span><select value={postulacionId} onChange={(event) => setPostulacionId(event.target.value)} className="h-11 w-full rounded-xl border border-slate-200 bg-white px-3 text-sm outline-none focus:border-brand focus:ring-4 focus:ring-blue-100">{postulaciones.map((postulacion) => <option key={postulacion.idPostulacion} value={postulacion.idPostulacion}>{postulacion.tituloProyecto} · {postulacion.folioPostulacion}</option>)}</select></label>
              <label className="block"><span className="mb-2 block text-sm font-semibold">Tipo de documento</span><select value={tipoDocumento} onChange={(event) => setTipoDocumento(event.target.value as Documento["tipoDocumento"])} className="h-11 w-full rounded-xl border border-slate-200 bg-white px-3 text-sm outline-none focus:border-brand focus:ring-4 focus:ring-blue-100"><option value="ANTEPROYECTO">Anteproyecto</option><option value="REPORTE_BIMESTRAL">Reporte bimestral</option><option value="REPORTE_FINAL">Reporte final</option></select></label>
              <label className="block"><span className="mb-2 block text-sm font-semibold">Archivo PDF</span><input id="archivo" type="file" accept="application/pdf,.pdf" aria-invalid={Boolean(errorArchivo)} onChange={(event) => { setArchivo(event.target.files?.[0] ?? null); setError(""); }} className={`block w-full rounded-xl border p-2 text-sm file:mr-3 file:rounded-lg file:border-0 file:bg-blue-50 file:px-3 file:py-2 file:text-xs file:font-semibold file:text-brand ${errorArchivo ? "border-rose-400" : "border-slate-200"}`}/>{errorArchivo ? <span role="status" className="mt-1 block text-xs text-rose-600">{errorArchivo}</span> : <span className="mt-1 block text-xs text-slate-400">PDF hasta 10 MB. La validación final también se realiza en el servidor.</span>}</label>
              {error && <p role="alert" className="rounded-lg bg-rose-50 p-3 text-sm text-rose-700">{error}</p>}
              <button disabled={subiendo || !archivo || Boolean(errorArchivo)} className="h-11 w-full rounded-xl bg-brand px-4 text-sm font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60">{subiendo ? "Subiendo documento…" : "Subir al expediente ↑"}</button>
            </form>}
          </section>

          <section className="rounded-2xl border border-slate-200 bg-white p-6 shadow-card">
            <div className="flex items-center justify-between"><div><h2 className="text-lg font-bold">Documentos entregados</h2><p className="mt-1 text-sm text-slate-500">{documentos.length} {documentos.length === 1 ? "archivo" : "archivos"} en tu expediente</p></div><span className="grid h-10 w-10 place-items-center rounded-xl bg-blue-50 text-lg text-brand">▤</span></div>
            {cargando ? <div className="mt-6 h-48 animate-pulse rounded-xl bg-slate-100"/> : documentos.length === 0 ? <div className="mt-6 rounded-xl border border-dashed border-slate-200 px-4 py-12 text-center"><p className="text-sm font-semibold">Aún no has subido documentos</p><p className="mt-1 text-xs text-slate-500">Tus entregables aparecerán aquí después de cargarlos.</p></div> : <div className="mt-5 space-y-3">{documentos.map((documento) => <article key={documento.idDocumento} className="rounded-xl border border-slate-100 p-4"><div className="flex items-start justify-between gap-3"><div><p className="text-sm font-semibold">{tipoDocumentoTexto[documento.tipoDocumento]}</p><p className="mt-1 text-xs text-slate-400">{tituloPorPostulacion.get(documento.idPostulacion) ?? `Postulación ${documento.idPostulacion}`} · {documento.folioDocumento}</p></div><span className={`shrink-0 rounded-full px-2.5 py-1 text-[10px] font-semibold ${documento.estadoAprobacion === "APROBADO" ? "bg-emerald-50 text-emerald-700" : documento.estadoAprobacion === "RECHAZADO" ? "bg-rose-50 text-rose-700" : "bg-amber-50 text-amber-700"}`}>{estadoDocumentoTexto[documento.estadoAprobacion]}</span></div><div className="mt-3 flex items-center justify-between"><span className="text-xs text-slate-400">{new Date(`${documento.fechaSubida}T12:00:00`).toLocaleDateString("es-MX", { dateStyle: "medium" })}</span><a href={`/api/documentos/${documento.idDocumento}/archivo`} className="text-xs font-semibold text-brand">Descargar ↓</a></div></article>)}</div>}
          </section>
        </div>
        {!cargando && error && postulaciones.length === 0 && <p role="alert" className="mt-4 rounded-xl bg-rose-50 p-3 text-sm text-rose-700">{error}</p>}
      </div>
    </main>
  );
}
