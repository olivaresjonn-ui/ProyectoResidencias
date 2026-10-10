package com.practica.webAvanzada.models.dto;

import java.util.List;
import java.util.Map;

public record AnaliticaResumen(
        long proyectosRegistrados,
        long vacantesAbiertas,
        long totalPostulaciones,
        int tecnologiasDistintas,
        List<TecnologiaDemanda> tecnologias,
        Map<String, Long> postulacionesPorEstado) {
}
