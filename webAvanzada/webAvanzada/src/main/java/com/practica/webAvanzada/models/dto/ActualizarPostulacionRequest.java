package com.practica.webAvanzada.models.dto;

import com.practica.webAvanzada.models.enums.EstadoPostulacion;

import jakarta.validation.constraints.NotNull;

public record ActualizarPostulacionRequest(@NotNull EstadoPostulacion estadoPostulacion, String comentarios) {
}
