export type Proyecto = {
  idProyecto: number;
  codigoProyecto: string;
  titulo: string;
  descripcion: string;
  objetivo: string;
  tecnologiasRequeridas: string;
  cupoAlumnos: number;
  estado: "DISPONIBLE" | "ASIGNADO" | "CERRADO";
  fechaPublicacion: string;
  empresa?: { razonSocial?: string; sector?: string };
};

export type SesionUsuario = {
  idUsuario: number;
  correo: string;
  rol: "ADMIN" | "ALUMNO" | "EMPRESA" | "ASESOR";
};

export class ApiError extends Error {
  constructor(message: string, readonly status: number) {
    super(message);
  }
}

async function getJson<T>(path: string): Promise<T> {
  const response = await fetch(path, { cache: "no-store" });
  if (!response.ok) {
    throw new ApiError(`La API respondió con el estado ${response.status}.`, response.status);
  }
  return response.json() as Promise<T>;
}

export async function obtenerProyectos(): Promise<Proyecto[]> {
  // Se consulta la ruta proxy de Next.js para evitar exponer la URL del backend
  // y no depender de CORS en el navegador durante el desarrollo.
  return getJson<Proyecto[]>("/api/proyectos");
}

export function obtenerSesion(): Promise<SesionUsuario> {
  return getJson<SesionUsuario>("/api/auth/me");
}
