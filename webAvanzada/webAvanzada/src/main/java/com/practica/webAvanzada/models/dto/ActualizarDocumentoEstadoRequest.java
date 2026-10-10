package com.practica.webAvanzada.models.dto;

import com.practica.webAvanzada.models.enums.EstadoAprobacion;

import jakarta.validation.constraints.NotNull;

public record ActualizarDocumentoEstadoRequest(@NotNull EstadoAprobacion estadoAprobacion) {
}
