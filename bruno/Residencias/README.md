# Pruebas Bruno: Residencias

Esta colección permite observar el flujo de autenticación sin escribir correos, contraseñas ni JWT en archivos del repositorio.

## Ejecutar

1. Abre en Bruno la carpeta `bruno/Residencias` como colección.
2. Inicia PostgreSQL y Spring Boot en el puerto `8080`.
3. Usa **Run collection** en orden. La primera petición solicita correo y contraseña como valores de prompt; Bruno no los guarda en el `.bru`.
4. El script de respuesta guarda el JWT como variable de ejecución en memoria para que las peticiones de sesión, autorización y catálogo lo reutilicen.
5. La última petición llama directamente a FastAPI con dos proyectos sintéticos; no usa información de la base de datos.

## Leer las respuestas

- `POST /api/auth/login`: `200` significa que la cuenta activa y la contraseña coincidieron; `401` significa credenciales inválidas.
- `GET /api/auth/me`: debe responder `200` y mostrar el rol del token.
- `GET /api/usuarios`: devuelve `200` para `ADMIN` y `403` para alumno, asesor o empresa. Sin token devuelve `401`.
- `GET /api/proyectos`: devuelve los proyectos permitidos para el rol de la sesión.
- `POST http://localhost:8001/recomendar`: demuestra TF-IDF y similitud coseno con datos inventados.

El último request verifica que el backend aplique el rol; un `403` con una sesión válida es el resultado esperado para las cuentas no administrativas.
