package com.practica.webAvanzada.models.dto;

public record AuthResponse(String accessToken, String tokenType, long expiresInSeconds, UsuarioResumen usuario) {
}
