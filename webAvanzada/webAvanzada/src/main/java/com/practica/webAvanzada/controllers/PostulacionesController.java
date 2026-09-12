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

import com.practica.webAvanzada.models.Postulaciones;
import com.practica.webAvanzada.services.PostulacionesService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/postulaciones")
@RequiredArgsConstructor
public class PostulacionesController {

    private final PostulacionesService postulacionesService;

    @PostMapping
    public ResponseEntity<Postulaciones> crear(@RequestBody Postulaciones postulacion) {
        return ResponseEntity.status(201).body(postulacionesService.crear(postulacion));
    }

    @GetMapping
    public ResponseEntity<List<Postulaciones>> obtenerTodas() {
        return ResponseEntity.ok(postulacionesService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Postulaciones> obtenerPorId(@PathVariable Long id) {
        return postulacionesService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Postulaciones> actualizar(
            @PathVariable Long id, @RequestBody Postulaciones postulacion) {
        Postulaciones actualizada = postulacionesService.actualizar(id, postulacion);
        return actualizada == null
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        return postulacionesService.eliminar(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
