package com.practica.webAvanzada.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.practica.webAvanzada.models.dto.ActualizarProyectoRequest;
import com.practica.webAvanzada.models.dto.CrearProyectoRequest;
import com.practica.webAvanzada.models.dto.ProyectoResumen;
import com.practica.webAvanzada.services.ProyectosResidenciaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/proyectos")
@RequiredArgsConstructor
public class ProyectosResidenciaController {

    private final ProyectosResidenciaService proyectosService;

    @PostMapping
    public ResponseEntity<ProyectoResumen> crear(
            @Valid @RequestBody CrearProyectoRequest proyecto,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(201).body(proyectosService.crear(
                proyecto, idUsuario(jwt), esAdministrador(jwt)));
    }

    @GetMapping
    public ResponseEntity<List<ProyectoResumen>> obtenerVisibles(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(proyectosService.obtenerVisibles(idUsuario(jwt),
                tieneRol(jwt, "EMPRESA"), tieneRol(jwt, "ADMIN") || tieneRol(jwt, "ASESOR")));
    }

    @GetMapping("/mios")
    public ResponseEntity<List<ProyectoResumen>> obtenerMios(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(proyectosService.obtenerMios(idUsuario(jwt)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProyectoResumen> obtenerPorId(@PathVariable("id") Long id, @AuthenticationPrincipal Jwt jwt) {
        return proyectosService.obtenerPorId(id, idUsuario(jwt), tieneRol(jwt, "EMPRESA"),
                tieneRol(jwt, "ADMIN") || tieneRol(jwt, "ASESOR"))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProyectoResumen> actualizar(
            @PathVariable("id") Long id,
            @Valid @RequestBody ActualizarProyectoRequest proyecto,
            @AuthenticationPrincipal Jwt jwt) {
        ProyectoResumen actualizado = proyectosService.actualizar(id, proyecto, idUsuario(jwt), esAdministrador(jwt));
        return actualizado == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id, @AuthenticationPrincipal Jwt jwt) {
        return proyectosService.eliminar(id, idUsuario(jwt), esAdministrador(jwt))
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    private Long idUsuario(Jwt jwt) {
        Number id = jwt.getClaim("idUsuario");
        return id.longValue();
    }

    private boolean esAdministrador(Jwt jwt) {
        return tieneRol(jwt, "ADMIN");
    }

    private boolean tieneRol(Jwt jwt, String rol) {
        List<String> authorities = jwt.getClaimAsStringList("authorities");
        return authorities != null && authorities.contains("ROLE_" + rol);
    }
}
