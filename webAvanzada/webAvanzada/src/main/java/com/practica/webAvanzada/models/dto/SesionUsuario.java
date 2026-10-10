package com.practica.webAvanzada.models.dto;

import com.practica.webAvanzada.models.enums.RolUser;

public record SesionUsuario(Long idUsuario, String correo, RolUser rol) {
}
