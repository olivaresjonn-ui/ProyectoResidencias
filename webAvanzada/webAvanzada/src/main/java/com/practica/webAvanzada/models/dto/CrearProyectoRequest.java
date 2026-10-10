package com.practica.webAvanzada.models.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CrearProyectoRequest(
        @NotBlank @Size(min = 3, max = 40) @Pattern(regexp = "[A-Za-z0-9][A-Za-z0-9_-]{2,39}") String codigoProyecto,
        @NotBlank @Size(min = 5, max = 160) String titulo,
        @NotBlank @Size(min = 20, max = 5000) String descripcion,
        @NotBlank @Size(min = 10, max = 5000) String objetivo,
        @NotBlank @Size(min = 2, max = 1000) String tecnologiasRequeridas,
        @NotNull @Positive @Max(100) Integer cupoAlumnos,
        Long idEmpresa) {
}
