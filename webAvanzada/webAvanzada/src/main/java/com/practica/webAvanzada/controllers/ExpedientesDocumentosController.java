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

import com.practica.webAvanzada.models.ExpedientesDocumentos;
import com.practica.webAvanzada.services.ExpedientesDocumentosService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/documentos")
@RequiredArgsConstructor
public class ExpedientesDocumentosController {

    private final ExpedientesDocumentosService expedientesDocumentosService;

    @PostMapping
    public ResponseEntity<ExpedientesDocumentos> crear(@RequestBody ExpedientesDocumentos documento) {
        return ResponseEntity.status(201).body(expedientesDocumentosService.crear(documento));
    }

    @GetMapping
    public ResponseEntity<List<ExpedientesDocumentos>> obtenerTodos() {
        return ResponseEntity.ok(expedientesDocumentosService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpedientesDocumentos> obtenerPorId(@PathVariable Long id) {
        return expedientesDocumentosService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpedientesDocumentos> actualizar(
            @PathVariable Long id, @RequestBody ExpedientesDocumentos documento) {
        ExpedientesDocumentos actualizada = expedientesDocumentosService.actualizar(id, documento);
        return actualizada == null
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        return expedientesDocumentosService.eliminar(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
