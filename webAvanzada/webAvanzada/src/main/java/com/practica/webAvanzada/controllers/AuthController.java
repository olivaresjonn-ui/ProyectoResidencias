package com.practica.webAvanzada.controllers;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.practica.webAvanzada.models.Usuario;
import com.practica.webAvanzada.models.dto.AuthResponse;
import com.practica.webAvanzada.models.dto.LoginRequest;
import com.practica.webAvanzada.models.dto.SesionUsuario;
import com.practica.webAvanzada.repositories.UsuarioRepositorio;
import com.practica.webAvanzada.services.JwtService;
import com.practica.webAvanzada.services.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;
    private final UsuarioRepositorio usuarioRepositorio;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest credenciales) {
        Usuario usuario = usuarioService.login(credenciales.correo(), credenciales.password());
        if (usuario == null) {
            return ResponseEntity.status(401).body("Credenciales inválidas.");
        }
        return ResponseEntity.ok(jwtService.emitirToken(usuario));
    }

    @GetMapping("/me")
    public ResponseEntity<SesionUsuario> sesionActual(@AuthenticationPrincipal Jwt jwt) {
        Number idUsuario = jwt.getClaim("idUsuario");
        Optional<Usuario> usuario = usuarioRepositorio.findById(idUsuario.longValue());
        return usuario.map(value -> ResponseEntity.ok(new SesionUsuario(
                        value.getIdUsuario(), value.getCorreo(), value.getRol())))
                .orElseGet(() -> ResponseEntity.status(401).build());
    }
}
