# Frontend de Residencias

Aplicación web de Next.js con App Router, TypeScript y Tailwind CSS.

## Preparación local

1. En `../webAvanzada/webAvanzada`, copia `config/database.properties.example` a `config/database.properties`, configura PostgreSQL y `JWT_SECRET`. Desde esa misma carpeta, arranca Spring Boot con `.\mvnw.cmd spring-boot:run`.
2. En `../ml-recomendador`, inicia FastAPI con Docker (`docker build -t nexo-ml .` y `docker run --rm -p 8001:8001 nexo-ml`) o sigue la alternativa de Python local en su [guía](../ml-recomendador/README.md).
3. En esta carpeta, copia `.env.example` a `.env.local` y ejecuta `npm install` y `npm run dev`.
4. Abre `http://localhost:3000/login`.

`BACKEND_BASE_URL` señala Spring Boot y `ML_BASE_URL` señala FastAPI. Si cambias estos valores, reinicia Next.js.

## Recorrido por rol

- **Alumno:** actualiza sus habilidades en `/perfil`, explora proyectos y recomendaciones personalizadas, se postula en `/postulaciones` y carga/consulta PDF en `/expediente`.
- **Empresa:** publica, edita, cierra y elimina sus proyectos desde `/empresa/proyectos`. Solo las empresas verificadas pueden publicar; los proyectos con postulaciones no se pueden eliminar.
- **Asesor:** desde `/asesor`, consulta alumnos asignados, cambia el estado de sus postulaciones y aprueba o rechaza sus documentos.
- **Administración:** desde `/admin/cuentas`, crea y gestiona cuentas; las altas de alumno, empresa y asesor guardan cuenta y perfil en una transacción. Las cuentas antiguas sin perfil muestran **Completar perfil**. En `/admin/empresas`, revisa empresas; en `/admin/asignaciones`, vincula alumnos con asesores; en `/admin/analitica`, revisa indicadores agregados. Al desactivar una cuenta se bloquean nuevos inicios de sesión, pero un JWT ya emitido puede seguir vigente hasta 60 minutos.

## Recorrido integrado de demostración

Usa cuentas y datos ficticios. No cargues expedientes reales durante la práctica.

1. **Prepara los servicios** siguiendo la sección anterior y configura el primer administrador si la base aún no contiene uno. Inicia sesión como `ADMIN`.
2. **Prepara perfiles:** en `/admin/cuentas`, crea una empresa, un asesor y un alumno. Anota las contraseñas temporales fuera del repositorio. En `/admin/empresas`, verifica la empresa; en `/admin/asignaciones`, asigna el alumno al asesor.
3. **Publica una vacante:** inicia sesión con la empresa y abre `/empresa/proyectos`. Crea un proyecto con cupo 1 y tecnologías como Java y PostgreSQL. Debe aparecer como disponible en el catálogo.
4. **Recomienda y postula:** inicia sesión como alumno, agrega Java y PostgreSQL en `/perfil` y vuelve al catálogo. La recomendación debe mostrar el proyecto y términos coincidentes; después, crea la postulación.
5. **Carga evidencia de práctica:** abre `/expediente`, asocia un PDF sintético con la postulación y súbelo. Debe aparecer con estado pendiente y poder descargarse con esa sesión.
6. **Revisa como asesor:** inicia sesión con el asesor asignado, revisa la postulación en `/asesor`, cambia su estado y aprueba o rechaza el documento. El alumno debe ver el estado actualizado al consultar de nuevo su expediente.
7. **Cierra el recorrido:** como administrador, consulta `/admin/analitica`. Debe reflejar el proyecto, su tecnología y el estado de la postulación sin exponer datos personales del alumno.

Para estudiar la implementación, sigue la cadena de inicio de sesión y las rutas por capas en la sección siguiente. Si un paso falla, anota la URL, el rol y el estado HTTP; revisa primero la ruta API de Next y después el controlador y servicio de Spring correspondientes.

El navegador solo llama al API de Next.js. El JWT permanece en una cookie `HttpOnly`; el servidor Next lo envía a Spring como Bearer. Next envía a FastAPI únicamente carrera, habilidades y campos descriptivos de los proyectos abiertos. El recomendador devuelve puntaje y términos coincidentes; no toma decisiones de selección.

## Ruta para familiarizarte con el código

Conviene seguir una operación de extremo a extremo y preguntar en cada capa: qué datos recibe, qué valida, a qué servicio llama y qué responde.

1. **Inicio de sesión:** `src/app/login/page.tsx` envía el formulario a `src/app/api/auth/login/route.ts`. Next guarda el JWT en una cookie `HttpOnly`; después Spring valida credenciales en `AuthController.java` y `UsuarioService.java`, lo firma en `JwtService.java` y decide permisos en `SecurityConfig.java`.
2. **Petición protegida:** desde `src/app/page.tsx`, sigue una llamada `/api/...` hacia su ruta BFF. `src/lib/server-proxy.ts` toma el token de la cookie y lo agrega como Bearer. El navegador nunca necesita leer el JWT.
3. **CRUD y base relacional:** usa proyectos como ejemplo: `src/app/empresa/proyectos/page.tsx` → `src/app/api/proyectos/...` → `ProyectosResidenciaController.java` → `ProyectosResidenciaService.java` → `ProyectosResidenciaRepositorio.java` → entidad `ProyectosResidencia.java` → PostgreSQL. Así se separan interfaz, HTTP, reglas de negocio y persistencia.
4. **Ciclo del alumno:** estudia `src/app/postulaciones/page.tsx` y `src/app/expediente/page.tsx`; compara sus peticiones con `PostulacionesService.java` y `ExpedientesDocumentosService.java`. Ahí están las reglas de cupo, propiedad de la postulación, autorización del documento y validación del PDF.
5. **Roles institucionales:** revisa `src/app/asesor/page.tsx` para el trabajo del asesor y las páginas `src/app/admin/asignaciones/page.tsx`, `src/app/admin/empresas/page.tsx`, `src/app/admin/cuentas/page.tsx` y `src/app/admin/analitica/page.tsx` para los flujos administrativos.
6. **Recomendación ML:** sigue `src/app/api/recomendaciones/route.ts` hasta `../ml-recomendador/main.py`. El frontend no conecta el modelo a PostgreSQL: Next le manda el perfil y las vacantes abiertas; FastAPI devuelve similitudes y términos coincidentes. El puntaje orienta, no es una probabilidad ni una decisión automática.
7. **Práctica pequeña:** al terminar cada etapa, elige un campo y localiza su recorrido desde el formulario hasta la columna de la entidad. Por ejemplo, `tecnologiasRequeridas` en proyectos o `habilidadesTecnicas` en el perfil del alumno.

El recorrido integral ya se ejecutó con datos ficticios en una base PostgreSQL temporal: se comprobaron la cookie `HttpOnly`, los cuatro roles, publicación, recomendación ML, postulación, carga y descarga de PDF, revisión del asesor y cambio de cupo. Los servicios temporales se apagaron y la base se eliminó al terminar. Falta repetir el recorrido con la configuración y las cuentas de tu PostgreSQL local. Para probar publicación, la empresa debe estar verificada; para probar el panel asesor, primero hay que asignarle alumnos.

## Dónde consume APIs el frontend

Las páginas cliente llaman rutas del mismo Next, por ejemplo `src/lib/api.ts` pide `/api/proyectos` y `src/app/login/page.tsx` envía el acceso a `/api/auth/login`. Estas rutas en `src/app/api/` son el puente (BFF): leen la cookie `residencias_token`, adjuntan `Authorization: Bearer ...` y reenvían a Spring usando `BACKEND_BASE_URL`. Spring responde desde sus controladores; servicios aplican reglas y repositorios consultan PostgreSQL.

La excepción es el recomendador: `src/app/recomendaciones.tsx` llama `/api/recomendaciones`; esa ruta de Next obtiene perfil y proyectos de Spring y envía los campos necesarios a `ML_BASE_URL/recomendar`. FastAPI no abre conexión a PostgreSQL.

Para iniciar todo, deja PostgreSQL activo; ejecuta `.\mvnw.cmd spring-boot:run` desde `../webAvanzada/webAvanzada`, `uvicorn main:app --reload --port 8001` desde `../ml-recomendador` con sus dependencias instaladas, y `npm run dev` desde esta carpeta. Las direcciones predeterminadas son Spring `http://localhost:8080`, ML `http://localhost:8001` y Next `http://localhost:3000`.
