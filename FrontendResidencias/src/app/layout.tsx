import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "ControlRes | Residencias profesionales",
  description: "Portal de proyectos de residencia profesional.",
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="es">
      <body>{children}</body>
    </html>
  );
}
