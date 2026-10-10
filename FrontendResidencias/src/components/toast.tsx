"use client";

import { useEffect, useRef } from "react";

export type ToastVariant = "success" | "error" | "info";

export function Toast({ message, variant = "success", onDismiss }: {
  message: string;
  variant?: ToastVariant;
  onDismiss: () => void;
}) {
  const dismissRef = useRef(onDismiss);

  useEffect(() => { dismissRef.current = onDismiss; }, [onDismiss]);
  useEffect(() => {
    const timer = window.setTimeout(() => dismissRef.current(), 4500);
    return () => window.clearTimeout(timer);
  }, [message]);

  const styles = {
    success: "border-emerald-200 bg-emerald-950 text-white",
    error: "border-rose-200 bg-rose-950 text-white",
    info: "border-blue-200 bg-slate-950 text-white",
  }[variant];
  const icon = variant === "success" ? "✓" : variant === "error" ? "!" : "i";

  return (
    <div
      role={variant === "error" ? "alert" : "status"}
      aria-live={variant === "error" ? "assertive" : "polite"}
      className={`fixed bottom-5 right-5 z-50 flex w-[min(92vw,390px)] items-start gap-3 rounded-2xl border px-4 py-3.5 shadow-2xl ${styles}`}
      style={{ animation: "toast-enter 180ms ease-out both" }}
    >
      <span className="grid h-6 w-6 shrink-0 place-items-center rounded-full bg-white/15 text-xs font-bold" aria-hidden="true">{icon}</span>
      <p className="flex-1 pt-0.5 text-sm leading-5">{message}</p>
      <button onClick={onDismiss} aria-label="Cerrar notificación" className="rounded-md px-1 text-lg leading-5 text-white/70 hover:bg-white/10 hover:text-white">×</button>
    </div>
  );
}
