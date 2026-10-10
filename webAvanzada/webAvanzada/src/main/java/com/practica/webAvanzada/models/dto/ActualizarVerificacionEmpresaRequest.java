package com.practica.webAvanzada.models.dto;

import com.practica.webAvanzada.models.enums.EstadoVerificacion;

import jakarta.validation.constraints.NotNull;

public record ActualizarVerificacionEmpresaRequest(@NotNull EstadoVerificacion estadoVerificacion) {
}
