"use client";

import { FormEvent, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { Toast } from "../../../components/toast";

type RolPerfil = "ALUMNO" | "EMPRESA" | "ASESOR";
type Usuario = { idUsuario: number; correo: string; rol: "ADMIN" | RolPerfil; activo: boolean };
type CuentaPendiente = { idUsuario: number; correo: string; rol: RolPerfil };
const CLAVE_PENDIENTE = "nexo-cuenta-pendiente";

const datosIniciales = {
  numeroControl: "", nombreCompleto: "", carrera: "", semestre: "7", habilidadesTecnicas: "",
  razonSocial: "", sector: "", telefono: "", direccion: "",
  claveAsesor: "", departamento: "", maximoAlumnos: "15",
};

export default function CuentasPage() {
  const router = useRouter();
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);
  const [alumnosConPerfil, setAlumnosConPerfil] = useState<Set<number>>(new Set());
  const [empresasConPerfil, setEmpresasConPerfil] = useState<Set<number>>(new Set());
  const [asesoresConPerfil, setAsesoresConPerfil] = useState<Set<number>>(new Set());
  const [rol, setRol] = useState<RolPerfil>("ALUMNO");
  const [correo, setCorreo] = useState("");
  const [password, setPassword] = useState("");
  const [datos, setDatos] = useState(datosIniciales);
  const [pendiente, setPendiente] = useState<CuentaPendiente | null>(null);
  const [cargando, setCargando] = useState(true);
  const [guardando, setGuardando] = useState(false);
  const [actualizandoId, setActualizandoId] = useState<number | null>(null);
  const [error, setError] = useState("");
  const [aviso, setAviso] = useState("");

  async function cargarUsuarios() {
    const [sesionRes, usuariosRes, alumnosRes, empresasRes, asesoresRes] = await Promise.all([
      fetch("/api/auth/me", { cache: "no-store" }),
      fetch("/api/usuarios", { cache: "no-store" }),
      fetch("/api/alumnos", { cache: "no-store" }),
      fetch("/api/empresas", { cache: "no-store" }),
      fetch("/api/asesores", { cache: "no-store" }),
    ]);
    if ([sesionRes, usuariosRes, alumnosRes, empresasRes, asesoresRes].some((response) => response.status === 401)) { router.replace("/login"); return; }
    const sesion = await sesionRes.json() as { idUsuario?: number; rol?: string };
    if (sesion.rol !== "ADMIN") { router.replace("/"); return; }
    if (!usuariosRes.ok) throw new Error("No se pudo consultar el directorio de cuentas.");
    if (!alumnosRes.ok) throw new Error("No se pudo consultar qué cuentas ya tienen perfil de alumno.");
    if (!empresasRes.ok || !asesoresRes.ok) throw new Error("No se pudo comprobar qué cuentas ya tienen perfil asociado.");
    setUsuarios(await usuariosRes.json() as Usuario[]);
    const perfiles = await alumnosRes.json() as Array<{ usuario?: { idUsuario?: number } }>;
    const empresas = await empresasRes.json() as Array<{ usuario?: { idUsuario?: number } }>;
    const asesores = await asesoresRes.json() as Array<{ usuario?: { idUsuario?: number } }>;
    const ids = (items: Array<{ usuario?: { idUsuario?: number } }>) => new Set(items.map((item) => item.usuario?.idUsuario).filter((id): id is number => Number.isInteger(id)));
    setAlumnosConPerfil(ids(perfiles));
    setEmpresasConPerfil(ids(empresas));
    setAsesoresConPerfil(ids(asesores));
  }

  useEffect(() => {
    const pendienteGuardado = sessionStorage.getItem(CLAVE_PENDIENTE);
    if (pendienteGuardado) {
      try {
        const cuenta = JSON.parse(pendienteGuardado) as CuentaPendiente;
        if (Number.isInteger(cuenta.idUsuario) && typeof cuenta.correo === "string"
          && ["ALUMNO", "EMPRESA", "ASESOR"].includes(cuenta.rol)) {
          setPendiente(cuenta);
          setRol(cuenta.rol);
        } else {
          sessionStorage.removeItem(CLAVE_PENDIENTE);
        }
      } catch {
        sessionStorage.removeItem(CLAVE_PENDIENTE);
      }
    }
    cargarUsuarios().catch((cause: unknown) => setError(cause instanceof Error ? cause.message : "Error de conexión."))
      .finally(() => setCargando(false));
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  async function cambiarEstado(usuario: Usuario) {
    setError(""); setAviso(""); setActualizandoId(usuario.idUsuario);
    try {
      const response = await fetch(`/api/usuarios/${usuario.idUsuario}`, {
        method: "PUT", headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ correo: usuario.correo, rol: usuario.rol, activo: !usuario.activo }),
      });
      if (response.status === 401) { router.replace("/login"); return; }
      if (!response.ok) throw new Error("No se pudo cambiar el estado de la cuenta.");
      setAviso(`${usuario.correo}: cuenta ${usuario.activo ? "desactivada" : "activada"}.`);
      await cargarUsuarios();
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "No se pudo contactar al backend.");
    } finally {
      setActualizandoId(null);
    }
  }

  async function crearPerfil(idUsuario: number, rolCuenta: RolPerfil) {
    const usuario = { idUsuario };
    let endpoint: string;
    let perfil: Record<string, unknown>;
    if (rolCuenta === "ALUMNO") {
      endpoint = "/api/alumnos";
      perfil = { numeroControl: datos.numeroControl.trim(), usuario, nombreCompleto: datos.nombreCompleto.trim(), carrera: datos.carrera.trim(), semestre: Number(datos.semestre), habilidadesTecnicas: datos.habilidadesTecnicas.trim(), estadoResidencia: "PENDIENTE" };
    } else if (rolCuenta === "EMPRESA") {
      endpoint = "/api/empresas";
      perfil = { usuario, razonSocial: datos.razonSocial.trim(), sector: datos.sector.trim(), nombreCompleto: datos.nombreCompleto.trim(), telefono: datos.telefono.trim(), direccion: datos.direccion.trim(), estadoVerificacion: "PENDIENTE" };
    } else {
      endpoint = "/api/asesores";
      perfil = { claveAsesor: datos.claveAsesor.trim(), usuario, nombreCompleto: datos.nombreCompleto.trim(), departamento: datos.departamento.trim(), maximoAlumnos: Number(datos.maximoAlumnos) };
    }
    const response = await fetch(endpoint, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(perfil) });
    if (!response.ok) {
      if (response.status === 409) throw new Error("Ya existe un perfil con ese identificador. Corrígelo y vuelve a intentar; la cuenta ya quedó creada.");
      if (response.status === 401) { router.replace("/login"); throw new Error("La sesión terminó."); }
      throw new Error("La cuenta se creó, pero no se pudo guardar el perfil. La cuenta quedó pendiente; corrige los datos y reintenta.");
    }
  }

  async function guardar(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setError(""); setAviso(""); setGuardando(true);
    try {
      let cuenta = pendiente;
      let perfilCreadoConCuenta = false;
      if (!cuenta) {
        const rutaCuenta: Record<RolPerfil, string> = {
          ALUMNO: "/api/usuarios/alumnos",
          EMPRESA: "/api/usuarios/empresas",
          ASESOR: "/api/usuarios/asesores",
        };
        const perfilInicial = rol === "ALUMNO"
          ? { numeroControl: datos.numeroControl.trim(), nombreCompleto: datos.nombreCompleto.trim(), carrera: datos.carrera.trim(), semestre: Number(datos.semestre), habilidadesTecnicas: datos.habilidadesTecnicas.trim() }
          : rol === "EMPRESA"
            ? { razonSocial: datos.razonSocial.trim(), sector: datos.sector.trim(), nombreCompleto: datos.nombreCompleto.trim(), telefono: datos.telefono.trim(), direccion: datos.direccion.trim() }
            : { claveAsesor: datos.claveAsesor.trim(), nombreCompleto: datos.nombreCompleto.trim(), departamento: datos.departamento.trim(), maximoAlumnos: Number(datos.maximoAlumnos) };
        const cuentaResponse = await fetch(rutaCuenta[rol], {
          method: "POST", headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ correo: correo.trim(), password, ...perfilInicial }),
        });
        if (cuentaResponse.status === 401) { router.replace("/login"); return; }
        if (cuentaResponse.status === 409) {
          const detalle = await cuentaResponse.json().catch(() => ({})) as { message?: string; detail?: string };
          setError(detalle.message ?? detalle.detail ?? "El correo o el número de control ya están registrados. Revisa el directorio y los datos del alumno.");
          return;
        }
        if (!cuentaResponse.ok) { setError("No se pudo crear la cuenta. Revisa el correo, contraseña y rol."); return; }
        const creada = await cuentaResponse.json() as Usuario;
        cuenta = { idUsuario: creada.idUsuario, correo: creada.correo, rol };
        perfilCreadoConCuenta = true;
      }
      if (!perfilCreadoConCuenta) await crearPerfil(cuenta.idUsuario, cuenta.rol);
      setAviso(`Se creó la cuenta ${cuenta.correo} y su perfil de ${cuenta.rol.toLowerCase()}.`);
      sessionStorage.removeItem(CLAVE_PENDIENTE);
      setPendiente(null); setCorreo(""); setPassword(""); setDatos(datosIniciales);
      await cargarUsuarios();
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "No se pudo completar el alta.");
    } finally {
      setGuardando(false);
    }
  }

  function seleccionarCuentaIncompleta(usuario: Usuario) {
    if (usuario.rol === "ADMIN") return;
    setPendiente({ idUsuario: usuario.idUsuario, correo: usuario.correo, rol: usuario.rol });
    setRol(usuario.rol);
    setDatos(datosIniciales);
    sessionStorage.setItem(CLAVE_PENDIENTE, JSON.stringify({ idUsuario: usuario.idUsuario, correo: usuario.correo, rol: usuario.rol }));
    setError("");
    setAviso("");
    document.getElementById("formulario-alta")?.scrollIntoView({ behavior: "smooth", block: "start" });
  }

  const asesores = usuarios.filter((usuario) => usuario.rol === "ASESOR").length;
  const empresas = usuarios.filter((usuario) => usuario.rol === "EMPRESA").length;
  const alumnos = usuarios.filter((usuario) => usuario.rol === "ALUMNO").length;

  return <main className="min-h-screen bg-canvas px-5 py-8 text-ink md:px-10 md:py-10">
    {aviso && <Toast message={aviso} onDismiss={() => setAviso("")} />}
    <div className="mx-auto max-w-7xl"><header className="flex flex-wrap items-center justify-between gap-4"><a href="/admin/asignaciones" className="flex items-center gap-3"><span className="grid h-10 w-10 place-items-center rounded-xl bg-brand font-bold text-white">N</span><span><b className="block">nexo · Administración</b><small className="text-slate-500">Cuentas y perfiles</small></span></a><nav className="flex flex-wrap gap-2"><a href="/admin/empresas" className="rounded-lg border border-slate-200 bg-white px-3 py-2 text-xs font-semibold text-slate-600">Empresas</a><a href="/admin/asignaciones" className="rounded-lg border border-slate-200 bg-white px-3 py-2 text-xs font-semibold text-slate-600">Asesores</a><a href="/admin/analitica" className="rounded-lg border border-slate-200 bg-white px-3 py-2 text-xs font-semibold text-slate-600">Analítica</a><button onClick={async () => { await fetch("/api/auth/logout", { method: "POST" }); router.replace("/login"); }} className="px-3 py-2 text-xs font-semibold text-slate-500">Cerrar sesión</button></nav></header>
      <div className="mt-10"><p className="text-xs font-bold uppercase tracking-[.16em] text-brand">Alta institucional</p><h1 className="mt-2 text-3xl font-bold tracking-tight">Crear cuenta y perfil</h1><p className="mt-2 max-w-2xl text-sm leading-6 text-slate-500">Elige el rol y completa sus datos. El servidor guarda la contraseña con hash y crea el perfil académico o empresarial vinculado a esa cuenta.</p></div>
      <section className="mt-6 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">{[["Cuentas", usuarios.length], ["Alumnos", alumnos], ["Empresas", empresas], ["Asesores", asesores]].map(([label, value]) => <article key={String(label)} className="rounded-2xl border border-slate-200 bg-white p-5"><p className="text-sm text-slate-500">{label}</p><p className="mt-2 text-3xl font-bold">{cargando ? "—" : value}</p></article>)}</section>
      <div className="mt-6 grid items-start gap-6 xl:grid-cols-[minmax(0,1fr)_390px]">
        <section className="rounded-2xl border border-slate-200 bg-white p-5 shadow-card md:p-6"><h2 className="text-lg font-bold">Directorio de cuentas</h2><p className="mt-1 text-sm text-slate-500">El secreto de acceso nunca se muestra en esta lista. Desactivar impide próximos inicios de sesión; las sesiones JWT vigentes pueden durar hasta 60 minutos.</p>{error && <p role="alert" className="mt-4 rounded-xl bg-rose-50 p-3 text-sm text-rose-700">{error}</p>}
          {cargando ? <div className="mt-5 h-48 animate-pulse rounded-xl bg-slate-100"/> : usuarios.length === 0 ? <p className="mt-5 rounded-xl border border-dashed border-slate-200 p-8 text-center text-sm text-slate-500">Aún no hay cuentas registradas.</p> : <div className="mt-5 divide-y divide-slate-100">{[...usuarios].sort((a, b) => b.idUsuario - a.idUsuario).map((usuario) => {
            const tienePerfil = usuario.rol === "ALUMNO" ? alumnosConPerfil.has(usuario.idUsuario)
              : usuario.rol === "EMPRESA" ? empresasConPerfil.has(usuario.idUsuario)
                : usuario.rol === "ASESOR" ? asesoresConPerfil.has(usuario.idUsuario) : true;
            return <article key={usuario.idUsuario} className="flex flex-wrap items-center justify-between gap-3 py-3"><div><p className="text-sm font-semibold">{usuario.correo}</p><p className="mt-1 text-[11px] text-slate-400">Cuenta #{usuario.idUsuario}</p></div><div className="flex flex-wrap items-center gap-2"><span className="rounded-full bg-blue-50 px-2.5 py-1 text-[10px] font-semibold text-blue-700">{usuario.rol}</span>{usuario.rol !== "ADMIN" && !tienePerfil && <button onClick={() => seleccionarCuentaIncompleta(usuario)} className="rounded-lg border border-amber-300 bg-amber-50 px-2.5 py-1.5 text-[10px] font-semibold text-amber-800 hover:bg-amber-100">Completar perfil</button>}<span className={`h-2 w-2 rounded-full ${usuario.activo ? "bg-emerald-500" : "bg-slate-300"}`} title={usuario.activo ? "Activa" : "Desactivada"}/>{usuario.rol !== "ADMIN" && <button disabled={actualizandoId === usuario.idUsuario} onClick={() => cambiarEstado(usuario)} className={`rounded-lg border px-2.5 py-1.5 text-[10px] font-semibold disabled:opacity-50 ${usuario.activo ? "border-rose-200 text-rose-700 hover:bg-rose-50" : "border-emerald-200 text-emerald-700 hover:bg-emerald-50"}`}>{actualizandoId === usuario.idUsuario ? "Guardando…" : usuario.activo ? "Desactivar" : "Activar"}</button>}</div></article>;
          })}</div>}
        </section>
        <aside id="formulario-alta" className="scroll-mt-6 rounded-2xl border border-slate-200 bg-white p-5 shadow-card md:p-6"><h2 className="text-lg font-bold">Alta de usuario</h2><form onSubmit={guardar} className="mt-5 space-y-4">
          <label className="block"><span className="mb-1.5 block text-xs font-semibold text-slate-700">Rol</span><select value={rol} disabled={Boolean(pendiente)} onChange={(event) => setRol(event.target.value as RolPerfil)} className="h-10 w-full rounded-lg border border-slate-200 bg-white px-3 text-sm"><option value="ALUMNO">Alumno</option><option value="EMPRESA">Empresa</option><option value="ASESOR">Asesor académico</option></select></label>
          {!pendiente ? <><label className="block"><span className="mb-1.5 block text-xs font-semibold text-slate-700">Correo electrónico</span><input required type="email" maxLength={254} autoComplete="email" value={correo} onChange={(event) => setCorreo(event.target.value)} className="h-10 w-full rounded-lg border border-slate-200 px-3 text-sm outline-none focus:border-brand focus:ring-4 focus:ring-blue-100" placeholder="persona@institucion.edu"/></label><label className="block"><span className="mb-1.5 block text-xs font-semibold text-slate-700">Contraseña temporal</span><input required type="password" minLength={8} maxLength={72} autoComplete="new-password" value={password} onChange={(event) => setPassword(event.target.value)} className="h-10 w-full rounded-lg border border-slate-200 px-3 text-sm outline-none focus:border-brand focus:ring-4 focus:ring-blue-100" placeholder="Al menos 8 caracteres"/><span className="mt-1 block text-[11px] text-slate-400">Entrégala por un canal seguro para que cambien su contraseña.</span></label></> : <div className="rounded-xl bg-amber-50 p-3 text-xs leading-5 text-amber-900">La cuenta <b>{pendiente.correo}</b> (#{pendiente.idUsuario}) ya existe. Completa su perfil para terminar el alta.</div>}
          {rol === "ALUMNO" && <div className="space-y-3 border-t border-slate-100 pt-4"><p className="text-xs font-bold uppercase tracking-wide text-slate-500">Perfil de alumno</p><Campo label="Número de control" value={datos.numeroControl} onChange={(value) => setDatos({ ...datos, numeroControl: value })}/><Campo label="Nombre completo" value={datos.nombreCompleto} onChange={(value) => setDatos({ ...datos, nombreCompleto: value })}/><Campo label="Carrera" value={datos.carrera} onChange={(value) => setDatos({ ...datos, carrera: value })}/><label className="block"><span className="mb-1.5 block text-xs font-semibold text-slate-700">Semestre</span><input required min="1" max="20" type="number" value={datos.semestre} onChange={(event) => setDatos({ ...datos, semestre: event.target.value })} className="h-10 w-full rounded-lg border border-slate-200 px-3 text-sm"/></label><label className="block"><span className="mb-1.5 block text-xs font-semibold text-slate-700">Habilidades iniciales (opcional)</span><textarea maxLength={1000} rows={2} value={datos.habilidadesTecnicas} onChange={(event) => setDatos({ ...datos, habilidadesTecnicas: event.target.value })} className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm" placeholder="Java, PostgreSQL, análisis de datos"/></label></div>}
          {rol === "EMPRESA" && <div className="space-y-3 border-t border-slate-100 pt-4"><p className="text-xs font-bold uppercase tracking-wide text-slate-500">Perfil de empresa</p><Campo label="Razón social" value={datos.razonSocial} onChange={(value) => setDatos({ ...datos, razonSocial: value })}/><Campo label="Sector" value={datos.sector} onChange={(value) => setDatos({ ...datos, sector: value })}/><Campo label="Contacto responsable" value={datos.nombreCompleto} onChange={(value) => setDatos({ ...datos, nombreCompleto: value })}/><Campo label="Teléfono" value={datos.telefono} onChange={(value) => setDatos({ ...datos, telefono: value })}/><Campo label="Dirección" value={datos.direccion} onChange={(value) => setDatos({ ...datos, direccion: value })}/><p className="rounded-lg bg-blue-50 p-3 text-xs leading-5 text-blue-900">El perfil inicia como pendiente. Debe verificarse antes de publicar vacantes.</p></div>}
          {rol === "ASESOR" && <div className="space-y-3 border-t border-slate-100 pt-4"><p className="text-xs font-bold uppercase tracking-wide text-slate-500">Perfil de asesor académico</p><Campo label="Clave de asesor" value={datos.claveAsesor} onChange={(value) => setDatos({ ...datos, claveAsesor: value })}/><Campo label="Nombre completo" value={datos.nombreCompleto} onChange={(value) => setDatos({ ...datos, nombreCompleto: value })}/><Campo label="Departamento" value={datos.departamento} onChange={(value) => setDatos({ ...datos, departamento: value })}/><label className="block"><span className="mb-1.5 block text-xs font-semibold text-slate-700">Máximo de alumnos</span><input required min="1" max="200" type="number" value={datos.maximoAlumnos} onChange={(event) => setDatos({ ...datos, maximoAlumnos: event.target.value })} className="h-10 w-full rounded-lg border border-slate-200 px-3 text-sm"/></label></div>}
          {error && <p role="alert" className="rounded-lg bg-rose-50 p-3 text-xs leading-5 text-rose-700">{error}</p>}
          <button disabled={guardando || cargando} className="h-11 w-full rounded-lg bg-brand text-sm font-semibold text-white hover:bg-blue-700 disabled:opacity-60">{guardando ? "Guardando…" : pendiente ? "Reintentar creación del perfil" : "Crear cuenta y perfil →"}</button>
        </form></aside>
      </div>
    </div>
  </main>;
}

function Campo({ label, value, onChange }: { label: string; value: string; onChange: (value: string) => void }) {
  return <label className="block"><span className="mb-1.5 block text-xs font-semibold text-slate-700">{label}</span><input required maxLength={200} value={value} onChange={(event) => onChange(event.target.value)} className="h-10 w-full rounded-lg border border-slate-200 px-3 text-sm outline-none focus:border-brand focus:ring-4 focus:ring-blue-100"/></label>;
}
