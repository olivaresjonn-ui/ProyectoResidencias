package com.practica.webAvanzada.models.dto;

import com.practica.webAvanzada.models.enums.RolUser;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ActualizarUsuarioRequest(
        @Email @Size(max = 254) String correo,
        @NotNull RolUser rol,
        @NotNull Boolean activo,
        @Size(min = 8, max = 72) String password) {
}
