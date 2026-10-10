package com.practica.webAvanzada.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import com.practica.webAvanzada.models.Empresa;
import com.practica.webAvanzada.models.dto.ActualizarVerificacionEmpresaRequest;
import com.practica.webAvanzada.services.EmpresaService;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;

    @GetMapping("/mia")
    public ResponseEntity<Empresa> obtenerMiPerfil(@AuthenticationPrincipal Jwt jwt) {
        Number idUsuario = jwt.getClaim("idUsuario");
        return empresaService.obtenerPorUsuario(idUsuario.longValue())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Empresa> crear(@RequestBody Empresa empresa) {
        return ResponseEntity.status(201).body(empresaService.crear(empresa));
    }

    @GetMapping
    public ResponseEntity<List<Empresa>> obtenerTodas() {
        return ResponseEntity.ok(empresaService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Empresa> obtenerPorId(@PathVariable("id") Long id) {
        return empresaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Empresa> actualizar(@PathVariable("id") Long id, @RequestBody Empresa empresa) {
        Empresa actualizada = empresaService.actualizar(id, empresa);
        return actualizada == null
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(actualizada);
    }

    @PatchMapping("/{id}/verificacion")
    public ResponseEntity<Empresa> actualizarVerificacion(
            @PathVariable("id") Long id,
            @Valid @RequestBody ActualizarVerificacionEmpresaRequest solicitud) {
        Empresa empresa = empresaService.actualizarVerificacion(id, solicitud.estadoVerificacion());
        return empresa == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(empresa);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        return empresaService.eliminar(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
