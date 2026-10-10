from typing import List

from fastapi import FastAPI
from pydantic import BaseModel, Field
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity

app = FastAPI(title="Nexo · Recomendador de residencias", version="1.0.0")


class Proyecto(BaseModel):
    idProyecto: int
    titulo: str
    descripcion: str = ""
    objetivo: str = ""
    tecnologiasRequeridas: str = ""


class Solicitud(BaseModel):
    carrera: str = ""
    habilidades: str = ""
    proyectos: List[Proyecto] = Field(default_factory=list)
    limite: int = Field(default=5, ge=1, le=20)


@app.get("/health")
def health():
    return {"status": "ok", "model": "tfidf-cosine-v1"}


@app.post("/recomendar")
def recomendar(solicitud: Solicitud):
    if not solicitud.proyectos:
        return {"modelo": "tfidf-cosine-v1", "recomendaciones": []}

    perfil = f"{solicitud.carrera} {solicitud.habilidades}".strip()
    textos_proyectos = [
        f"{p.titulo} {p.descripcion} {p.objetivo} {p.tecnologiasRequeridas}".strip()
        for p in solicitud.proyectos
    ]
    vectorizador = TfidfVectorizer(
        strip_accents="unicode",
        token_pattern=r"(?u)\b\w+\b",
        ngram_range=(1, 2),
    )
    analizar = vectorizador.build_analyzer()
    tokens_perfil = analizar(perfil)
    tokens_proyectos = [analizar(texto) for texto in textos_proyectos]
    if not any(tokens_perfil) and not any(any(tokens) for tokens in tokens_proyectos):
        return {
            "modelo": "tfidf-cosine-v1",
            "recomendaciones": [
                {"idProyecto": proyecto.idProyecto, "puntaje": 0.0, "motivos": []}
                for proyecto in solicitud.proyectos[: solicitud.limite]
            ],
        }

    matriz = vectorizador.fit_transform([perfil] + textos_proyectos)
    similitudes = cosine_similarity(matriz[0:1], matriz[1:]).flatten()
    terminos_perfil = set(tokens_perfil)

    resultados = []
    for proyecto, score, tokens in zip(solicitud.proyectos, similitudes, tokens_proyectos):
        terminos_proyecto = set(tokens)
        coincidencias = sorted(terminos_perfil & terminos_proyecto)[:6]
        resultados.append({
            "idProyecto": proyecto.idProyecto,
            "puntaje": round(float(score) * 100, 1),
            "motivos": coincidencias,
        })

    resultados.sort(key=lambda item: item["puntaje"], reverse=True)
    return {"modelo": "tfidf-cosine-v1", "recomendaciones": resultados[: solicitud.limite]}
