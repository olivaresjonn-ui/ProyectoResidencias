package com.practica.webAvanzada.models.dto;

import com.practica.webAvanzada.models.enums.EstadoProyecto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;

public record ActualizarProyectoRequest(
        @NotBlank @Size(min = 5, max = 160) String titulo,
        @NotBlank @Size(min = 20, max = 5000) String descripcion,
        @NotBlank @Size(min = 10, max = 5000) String objetivo,
        @NotBlank @Size(min = 2, max = 1000) String tecnologiasRequeridas,
        @NotNull @Positive @Max(100) Integer cupoAlumnos,
        @NotNull EstadoProyecto estado) {
}
