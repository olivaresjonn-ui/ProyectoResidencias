package com.practica.webAvanzada.models.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearCuentaAsesorRequest(
        @NotBlank @Email @Size(max = 254) String correo,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(max = 200) String claveAsesor,
        @NotBlank @Size(max = 200) String nombreCompleto,
        @NotBlank @Size(max = 200) String departamento,
        @NotNull @Min(1) @Max(200) Integer maximoAlumnos) {
}
