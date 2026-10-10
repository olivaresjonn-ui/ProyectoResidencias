package com.practica.webAvanzada.models.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearCuentaEmpresaRequest(
        @NotBlank @Email @Size(max = 254) String correo,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(max = 200) String razonSocial,
        @NotBlank @Size(max = 200) String sector,
        @NotBlank @Size(max = 200) String nombreCompleto,
        @NotBlank @Size(max = 200) String telefono,
        @NotBlank @Size(max = 200) String direccion) {
}
