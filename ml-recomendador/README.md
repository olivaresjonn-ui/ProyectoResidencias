# Microservicio recomendador

Servicio HTTP independiente con FastAPI. Usa TF-IDF para representar el perfil del alumno y el texto del proyecto como vectores, y similitud coseno para ordenar las vacantes más parecidas. Es un recomendador por contenido: el porcentaje es una puntuación relativa de similitud, no una probabilidad de contratación ni una decisión automática.

## Ejecutar con Docker

Desde esta carpeta:

```powershell
docker build -t nexo-ml .
docker run --rm -p 8001:8001 nexo-ml
```

## Ejecutar con Python local

Requiere Python 3.12 o compatible con las versiones fijadas en `requirements.txt`.

```powershell
py -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
uvicorn main:app --reload --port 8001
```

La API de FastAPI y su esquema interactivo aparecen en `http://localhost:8001/docs`; `GET /health` confirma que el proceso está activo. La aplicación web usa la URL de `ML_BASE_URL` (por defecto `http://localhost:8001`).

## Contrato de recomendación

`POST /recomendar` recibe un perfil breve y proyectos que el backend transaccional ya filtró como disponibles:

```json
{
  "carrera": "Ingeniería en Sistemas",
  "habilidades": "Java, PostgreSQL, C, APIs REST",
  "limite": 5,
  "proyectos": [
    {
      "idProyecto": 42,
      "titulo": "API para inventario",
      "descripcion": "Desarrollar servicios para el control de productos",
      "objetivo": "Publicar una API segura y documentada",
      "tecnologiasRequeridas": "Java, PostgreSQL, Spring"
    }
  ]
}
```

La respuesta devuelve `modelo`, `idProyecto`, `puntaje` de 0 a 100 y `motivos` con los términos compartidos. Next.js usa el ID para volver a asociar título y empresa a partir de la respuesta de Spring; correo, contraseña y archivos no se envían al servicio ML.

## Flujo de aprendizaje

1. `Proyecto` y `Solicitud` describen y validan el JSON de entrada con Pydantic.
2. `TfidfVectorizer` transforma textos en vectores numéricos que reflejan términos relevantes en cada documento.
3. `cosine_similarity` compara el perfil contra cada proyecto.
4. Se ordenan los puntajes y se devuelven coincidencias para que la recomendación pueda explicarse en la interfaz.

El modelo no está entrenado con historiales de contratación. Es un baseline reproducible y explicable; con datos etiquetados de postulaciones exitosas se podría evaluar y entrenar un modelo supervisado como una etapa posterior.
