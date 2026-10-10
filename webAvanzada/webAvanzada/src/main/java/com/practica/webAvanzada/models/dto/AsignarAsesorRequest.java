package com.practica.webAvanzada.models.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AsignarAsesorRequest(@NotNull @Positive Long idAsesor) {
}
