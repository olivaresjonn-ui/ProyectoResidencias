# Plataforma de Residencias Profesionales

Aplicación para gestionar perfiles, proyectos, postulaciones y documentos de residencias, con acceso por rol y recomendaciones de proyectos.

## Estructura

| Carpeta | Función |
| --- | --- |
| `FrontendResidencias` | Interfaz Next.js y Tailwind CSS; rutas que consumen las APIs. |
| `webAvanzada/webAvanzada` | Backend Spring Boot con autenticación JWT y acceso a PostgreSQL. |
| `ml-recomendador` | Microservicio FastAPI con recomendaciones mediante TF-IDF y similitud coseno. |
| `bruno/Residencias` | Peticiones para consultar las APIs desde Bruno. |

## Ejecutar localmente

Requiere Java 21, Node.js compatible con Next.js 15, Python compatible con `ml-recomendador/requirements.txt` y PostgreSQL. Inicia PostgreSQL y prepara la base `residencias` con las tablas del backend. El esquema se valida al iniciar; revisa la migración en `webAvanzada/webAvanzada/database/migrations` si falta la relación alumno-asesor.

Abre tres terminales desde la raíz del proyecto.

### 1. Backend transaccional — puerto 8080

```powershell
cd webAvanzada/webAvanzada
# Solo la primera vez; conserva tu archivo local si ya existe:
if (!(Test-Path config/database.properties)) {
    Copy-Item config/database.properties.example config/database.properties
}
# Configura PostgreSQL y JWT_SECRET en config/database.properties.
.\mvnw.cmd spring-boot:run
```

Consulta [la configuración del backend](webAvanzada/webAvanzada/README-CONFIGURACION.md) para generar la clave JWT y configurar el primer ADMIN.

### 2. Microservicio de Machine Learning — puerto 8001

```powershell
cd ml-recomendador
py -m venv .venv
.\.venv\Scripts\python.exe -m pip install -r requirements.txt
.\.venv\Scripts\python.exe -m uvicorn main:app --port 8001
```

Documentación interactiva: [http://localhost:8001/docs](http://localhost:8001/docs). El servicio recibe carrera, habilidades y proyectos; devuelve un orden por similitud de texto. El puntaje no representa una probabilidad de contratación.

### 3. Frontend — puerto 3000

```powershell
cd FrontendResidencias
if (!(Test-Path .env.local)) { Copy-Item .env.example .env.local }
npm ci
npm run dev
```

Abre [http://localhost:3000/login](http://localhost:3000/login). Usa las cuentas de la base de datos. Las cuentas de alumnos requieren un perfil asociado para mostrar sus datos y recomendaciones; administración puede completar los perfiles faltantes.

## Dónde se consumen las APIs

Las páginas llaman a rutas `/api` de Next.js. Estas rutas leen la sesión y llaman al backend Spring Boot con el token JWT. Spring consulta PostgreSQL mediante servicios y repositorios.

| Archivo | Qué hace |
| --- | --- |
| `FrontendResidencias/src/app/login/page.tsx` | Envía el formulario a `/api/auth/login`. |
| `FrontendResidencias/src/app/api/auth/login/route.ts` | Consume el login de Spring y guarda la sesión en una cookie HttpOnly. |
| `FrontendResidencias/src/lib/server-proxy.ts` | Reenvía peticiones a `BACKEND_BASE_URL` con autenticación. |
| `FrontendResidencias/src/app/api/recomendaciones/route.ts` | Obtiene el perfil y proyectos de Spring; llama a `ML_BASE_URL/recomendar`. |
| `ml-recomendador/main.py` | Implementa la API de recomendaciones. |

Las URLs se configuran en `FrontendResidencias/.env.local`: `BACKEND_BASE_URL=http://localhost:8080` y `ML_BASE_URL=http://localhost:8001`.

## Archivos locales

Git excluye las credenciales, dependencias, compilaciones y documentos cargados. Las plantillas `.env.example` y `database.properties.example` permiten configurar otra computadora. La base de datos y sus usuarios no se incluyen en el repositorio.
