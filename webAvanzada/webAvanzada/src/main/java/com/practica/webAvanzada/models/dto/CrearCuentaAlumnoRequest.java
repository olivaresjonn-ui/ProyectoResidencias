package com.practica.webAvanzada.models.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearCuentaAlumnoRequest(
        @NotBlank @Email @Size(max = 254) String correo,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(max = 32) String numeroControl,
        @NotBlank @Size(max = 200) String nombreCompleto,
        @NotBlank @Size(max = 200) String carrera,
        @NotNull @Min(1) @Max(20) Integer semestre,
        @Size(max = 1000) String habilidadesTecnicas) {
}
