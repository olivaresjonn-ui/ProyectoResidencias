package com.practica.webAvanzada.models.dto;

import com.practica.webAvanzada.models.enums.RolUser;

public record UsuarioResumen(Long idUsuario, String correo, RolUser rol) {
}
