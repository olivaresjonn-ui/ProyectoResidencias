"use client";

import { FormEvent, useState } from "react";

export default function LoginPage() {
  const [correo, setCorreo] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [cargando, setCargando] = useState(false);

  async function iniciarSesion(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setCargando(true);
    try {
      const response = await fetch("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ correo, password }),
      });
      if (!response.ok) {
        setError(response.status === 401 ? "El correo o la contraseña no son correctos." : "No se pudo iniciar sesión. Revisa la conexión con el servidor.");
        return;
      }
      window.location.assign("/");
    } catch {
      setError("No se pudo conectar con el servidor. Confirma que Spring Boot esté iniciado.");
    } finally {
      setCargando(false);
    }
  }

  return (
    <main className="grid min-h-screen grid-cols-1 bg-white lg:grid-cols-[1.05fr_.95fr]">
      <section className="relative hidden overflow-hidden bg-[#172b4d] p-12 text-white lg:flex lg:flex-col lg:justify-between xl:p-16">
        <div className="absolute -right-24 top-20 h-[460px] w-[460px] rounded-full border border-white/10"/><div className="absolute -right-4 top-40 h-[340px] w-[340px] rounded-full border border-white/10"/>
        <a href="/" className="relative flex items-center gap-3"><span className="grid h-10 w-10 place-items-center rounded-xl bg-white text-lg font-bold text-brand">N</span><span className="text-xl font-bold tracking-tight">nexo</span></a>
        <div className="relative max-w-xl pb-10"><span className="rounded-full bg-white/10 px-3 py-1.5 text-[11px] font-semibold tracking-[.15em] text-blue-100">TU EXPERIENCIA PROFESIONAL COMIENZA AQUÍ</span><h1 className="mt-7 text-4xl font-semibold leading-tight xl:text-5xl">Conecta tu talento con oportunidades reales.</h1><p className="mt-5 max-w-lg text-base leading-7 text-blue-100/75">Encuentra proyectos, acompaña tu proceso y construye experiencia en el campo que te apasiona.</p><div className="mt-10 flex gap-3"><span className="h-1.5 w-8 rounded-full bg-white"/><span className="h-1.5 w-3 rounded-full bg-white/30"/><span className="h-1.5 w-3 rounded-full bg-white/30"/></div></div>
        <p className="relative text-xs text-blue-100/60">Portal institucional de residencias profesionales · 2026</p>
      </section>

      <section className="flex min-h-screen min-w-0 items-center justify-center px-6 py-12">
        <div className="w-full max-w-[420px]">
          <a href="/" className="mb-12 flex items-center gap-2.5 lg:hidden"><span className="grid h-9 w-9 place-items-center rounded-xl bg-brand font-bold text-white">N</span><span className="text-lg font-bold text-ink">nexo</span></a>
          <div className="mb-9"><p className="text-sm font-semibold text-brand">Bienvenido de vuelta</p><h2 className="mt-2 text-3xl font-bold tracking-tight text-ink">Inicia sesión</h2><p className="mt-2 text-sm leading-6 text-slate-500">Ingresa con la cuenta institucional que registraste para el sistema de residencias.</p></div>
          <form onSubmit={iniciarSesion} className="space-y-5">
            <label className="block"><span className="mb-2 block text-sm font-semibold text-slate-700">Correo electrónico</span><input autoComplete="username" type="email" required value={correo} onChange={(event) => setCorreo(event.target.value)} placeholder="nombre@institucion.edu" className="h-12 w-full rounded-xl border border-slate-200 px-4 text-sm outline-none transition placeholder:text-slate-400 focus:border-brand focus:ring-4 focus:ring-blue-100"/></label>
            <label className="block"><span className="mb-2 block text-sm font-semibold text-slate-700">Contraseña</span><input autoComplete="current-password" type="password" required minLength={3} value={password} onChange={(event) => setPassword(event.target.value)} placeholder="Ingresa tu contraseña" className="h-12 w-full rounded-xl border border-slate-200 px-4 text-sm outline-none transition placeholder:text-slate-400 focus:border-brand focus:ring-4 focus:ring-blue-100"/></label>
            {error && <p role="alert" className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</p>}
            <button disabled={cargando} className="flex h-12 w-full items-center justify-center gap-2 rounded-xl bg-brand text-sm font-semibold text-white shadow-lg shadow-blue-900/15 transition hover:bg-blue-700 disabled:cursor-wait disabled:opacity-70">{cargando ? <><span className="h-4 w-4 animate-spin rounded-full border-2 border-white/40 border-t-white"/>Verificando...</> : <>Entrar al portal <span>→</span></>}</button>
          </form>
          <p className="mt-8 text-center text-xs leading-5 text-slate-400">¿No puedes acceder? Contacta a la coordinación de residencias de tu institución.</p>
          <p className="mt-12 text-center text-xs text-slate-400">Acceso protegido · Tus datos se usan para gestionar tu proceso académico.</p>
        </div>
      </section>
    </main>
  );
}
