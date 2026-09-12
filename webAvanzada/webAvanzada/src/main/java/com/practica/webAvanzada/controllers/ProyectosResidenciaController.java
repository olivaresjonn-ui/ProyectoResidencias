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

import com.practica.webAvanzada.models.ProyectosResidencia;
import com.practica.webAvanzada.services.ProyectosResidenciaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/proyectos")
@RequiredArgsConstructor
public class ProyectosResidenciaController {

    private final ProyectosResidenciaService proyectosResidenciaService;

    @PostMapping
    public ResponseEntity<ProyectosResidencia> crear(@RequestBody ProyectosResidencia proyecto) {
        return ResponseEntity.status(201).body(proyectosResidenciaService.crear(proyecto));
    }

    @GetMapping
    public ResponseEntity<List<ProyectosResidencia>> obtenerTodos() {
        return ResponseEntity.ok(proyectosResidenciaService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProyectosResidencia> obtenerPorId(@PathVariable Long id) {
        return proyectosResidenciaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProyectosResidencia> actualizar(
            @PathVariable Long id, @RequestBody ProyectosResidencia proyecto) {
        ProyectosResidencia actualizada = proyectosResidenciaService.actualizar(id, proyecto);
        return actualizada == null
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        return proyectosResidenciaService.eliminar(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
