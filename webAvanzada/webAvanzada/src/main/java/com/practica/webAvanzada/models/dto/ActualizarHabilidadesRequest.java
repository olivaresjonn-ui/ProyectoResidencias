package com.practica.webAvanzada.models.dto;

import jakarta.validation.constraints.Size;

public record ActualizarHabilidadesRequest(
        @Size(max = 1000, message = "Las habilidades no pueden superar 1000 caracteres.")
        String habilidadesTecnicas) {
}
