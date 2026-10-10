# Configuración local del backend

La contraseña de PostgreSQL no debe guardarse en `src/main/resources/application.properties` ni subirse al repositorio.

El valor predeterminado de `JPA_DDL_AUTO` es `validate`: Spring comprueba que las tablas y columnas esperadas existan, sin crear ni alterar el esquema. La comparación con el backend original encontró una diferencia conocida: la asignación opcional de asesor requiere `alumnos.id_asesor`. Revisa y ejecuta `database/migrations/001_vincular_alumnos_asesores.sql` en la base seleccionada desde pgAdmin antes de arrancar Spring. `update` está disponible para una base local desechable o después de revisar los cambios; `create` y `create-drop` recrean el esquema y no se deben usar con información que quieras conservar.

## Preparar conexión local

Desde `webAvanzada/webAvanzada`, copia la plantilla y edita el archivo local:

```powershell
Copy-Item config/database.properties.example config/database.properties
```

En `config/database.properties`, escribe las credenciales de tu PostgreSQL local y una clave JWT secreta. Ese archivo está excluido de Git. También puedes definir `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` y `JPA_DDL_AUTO` como variables de entorno; estas tienen prioridad sobre el archivo. La clave JWT debe ser Base64 y representar al menos 32 bytes aleatorios.

Para una base sin administradores, configura también `ADMIN_BOOTSTRAP_EMAIL` y `ADMIN_BOOTSTRAP_PASSWORD` en ese archivo local. Al iniciar, Spring crea una sola cuenta `ADMIN` activa si aún no existe; la contraseña se guarda con BCrypt. Si ya existe un administrador, estas variables no crean ni reemplazan cuentas. Usa una contraseña temporal segura y retira esas dos variables locales después del primer arranque. No agregues credenciales al repositorio.

Después inicia Spring Boot desde la carpeta del proyecto Maven con `./mvnw spring-boot:run` (en PowerShell, `.\mvnw.cmd spring-boot:run`).

`application.properties` conserva la URL y usuario por defecto para PostgreSQL local, pero requiere que la contraseña se configure en uno de los dos lugares anteriores.

Para generar la clave JWT en PowerShell:

```powershell
$bytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
[Convert]::ToBase64String($bytes)
```

Copia la salida como `JWT_SECRET` en `config/database.properties` y reinicia Spring. Debe conservarse entre arranques para que los JWT de las sesiones sigan siendo verificables. Si aparece `JWT_SECRET debe ser una clave codificada en Base64` o `Last unit does not have enough valid bits`, genera una clave nueva con el comando anterior y reemplaza el valor local; no pegues esa clave en el chat.

Copia el texto generado en `JWT_SECRET` de `config/database.properties`. El backend ahora entrega JWT de una hora y exige un token Bearer en las rutas privadas. El rol viaja en el token como autoridad `ROLE_*` y Spring Security lo usa en las reglas de acceso. La cuenta ADMIN inicial permite entrar a `/admin/cuentas` y dar de alta perfiles desde la interfaz.

## Flujo de autenticación

`POST /api/auth/login` recibe `{"correo":"...","password":"..."}`. Si la cuenta existe, está activa y la contraseña coincide, responde con un `accessToken`, su duración y un resumen del usuario. Para consultar rutas protegidas, se envía `Authorization: Bearer <accessToken>`. `GET /api/auth/me` devuelve la cuenta asociada al token vigente.

### Probar el acceso con Bruno

Puedes crear las siguientes peticiones manualmente en Bruno u otro cliente HTTP.

1. Crea una petición **POST** a `http://localhost:8080/api/auth/login`.
2. En **Body → JSON**, usa `{"correo":"correo-de-la-cuenta","password":"contraseña-de-la-cuenta"}`. Los nombres de las propiedades son `correo` y `password`.
3. Con credenciales válidas, la respuesta es `200` e incluye `accessToken`. En otra petición **GET** a `http://localhost:8080/api/auth/me`, selecciona **Auth → Bearer Token** y pega ese valor; la respuesta `200` muestra la sesión actual.
4. Para rutas protegidas, conserva la autenticación Bearer. `401` indica que falta un token válido o que el inicio de sesión falló; `403` indica que el token es válido, pero su rol no tiene permiso para esa ruta.

`GET /api/usuarios` requiere el rol `ADMIN`. Por eso responderá `401` sin token y `403` con un token de alumno, asesor o empresa. No uses esa ruta para verificar conectividad: `POST /api/auth/login` es la comprobación adecuada porque busca el correo en la tabla `usuarios`; un correo inexistente con formato válido devuelve `401` (“credenciales inválidas”), mientras que una caída de PostgreSQL provoca un error del servidor.

Las reglas del filtro limitan rutas según el rol y los servicios comprueban el propietario de la operación. Por ejemplo, una empresa solo administra los proyectos ligados al usuario de su perfil. Los alumnos y las empresas consultan sus perfiles; el asesor solo consulta a los alumnos que administración le asignó. Tras aplicar la migración, asigna asesores en `/admin/asignaciones` para dar acceso a las filas existentes.

## Postulaciones y documentos

- `POST /api/postulaciones` recibe `{ "idProyecto": 12 }`. El backend obtiene el alumno a partir del JWT, exige que el proyecto esté disponible y evita duplicados. Las aceptaciones consumen cupo; el proyecto pasa a `ASIGNADO` cuando se llena. El cupo y la revisión se serializan con un bloqueo transaccional por proyecto.
- `GET /api/postulaciones/mias` devuelve las solicitudes del alumno actual. Asesor y administración consultan sus colas permitidas; solo el asesor asignado puede revisar la postulación.
- `POST /api/documentos` recibe `multipart/form-data` con `idPostulacion`, `tipoDocumento` y `archivo`. Acepta PDF de hasta 10 MB, verifica la firma del archivo y lo guarda en `storage/documentos` con nombre aleatorio. PostgreSQL conserva solo los metadatos y la clave interna del archivo.
- `GET /api/documentos/{id}/archivo` exige autenticación y valida que el solicitante sea dueño, asesor asignado o administrador. `PATCH /api/documentos/{id}/estado` permite revisar aprobación al asesor asignado o administración. Administración no puede borrar postulaciones que ya tengan documentos ligados.

La empresa debe tener `estado_verificacion=VERIFICADA` antes de publicar. Administración puede asignar alumnos hasta el `maximo_alumnos` de cada asesor; si se alcanza ese límite, la API responde `409 Conflict`.
