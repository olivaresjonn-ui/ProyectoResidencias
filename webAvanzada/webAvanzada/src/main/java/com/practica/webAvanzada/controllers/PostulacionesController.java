package com.practica.webAvanzada.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.practica.webAvanzada.models.dto.ActualizarPostulacionRequest;
import com.practica.webAvanzada.models.dto.CrearPostulacionRequest;
import com.practica.webAvanzada.models.dto.PostulacionResumen;
import com.practica.webAvanzada.services.PostulacionesService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/postulaciones")
@RequiredArgsConstructor
public class PostulacionesController {

    private final PostulacionesService postulacionesService;

    @PostMapping
    public ResponseEntity<PostulacionResumen> crear(
            @Valid @RequestBody CrearPostulacionRequest solicitud,
            @AuthenticationPrincipal Jwt jwt) {
        Number idUsuario = jwt.getClaim("idUsuario");
        return ResponseEntity.status(201).body(postulacionesService.crear(solicitud, idUsuario.longValue()));
    }

    @GetMapping
    public ResponseEntity<List<PostulacionResumen>> obtenerTodas(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(postulacionesService.obtenerTodas(idUsuario(jwt), tieneRol(jwt, "ADMIN")));
    }

    @GetMapping("/mias")
    public ResponseEntity<List<PostulacionResumen>> obtenerMias(@AuthenticationPrincipal Jwt jwt) {
        Number idUsuario = jwt.getClaim("idUsuario");
        return ResponseEntity.ok(postulacionesService.obtenerMias(idUsuario.longValue()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostulacionResumen> obtenerPorId(@PathVariable("id") Long id, @AuthenticationPrincipal Jwt jwt) {
        return postulacionesService.obtenerPorId(id, idUsuario(jwt), tieneRol(jwt, "ADMIN"))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostulacionResumen> actualizar(
            @PathVariable("id") Long id,
            @Valid @RequestBody ActualizarPostulacionRequest postulacion,
            @AuthenticationPrincipal Jwt jwt) {
        PostulacionResumen actualizada = postulacionesService.actualizar(id, postulacion, idUsuario(jwt), tieneRol(jwt, "ADMIN"));
        return actualizada == null
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        return postulacionesService.eliminar(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    private Long idUsuario(Jwt jwt) {
        Number id = jwt.getClaim("idUsuario");
        return id.longValue();
    }

    private boolean tieneRol(Jwt jwt, String rol) {
        List<String> authorities = jwt.getClaimAsStringList("authorities");
        return authorities != null && authorities.contains("ROLE_" + rol);
    }
}
