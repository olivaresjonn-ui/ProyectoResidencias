package com.practica.webAvanzada.models.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CrearPostulacionRequest(@NotNull @Positive Long idProyecto) {
}
