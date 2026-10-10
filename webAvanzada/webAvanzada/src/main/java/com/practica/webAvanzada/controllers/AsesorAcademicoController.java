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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import com.practica.webAvanzada.models.AsesorAcademico;
import com.practica.webAvanzada.services.AsesorAcademicoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/asesores")
@RequiredArgsConstructor
public class AsesorAcademicoController {

    private final AsesorAcademicoService asesorAcademicoService;

    @GetMapping("/mio")
    public ResponseEntity<AsesorAcademico> obtenerMiPerfil(@AuthenticationPrincipal Jwt jwt) {
        Number idUsuario = jwt.getClaim("idUsuario");
        return asesorAcademicoService.obtenerAsesorPorUsuario(idUsuario.longValue())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AsesorAcademico> crearAsesor(@RequestBody AsesorAcademico asesorAcademico) {
        AsesorAcademico asesorCreado = asesorAcademicoService.crearAsesorAcademico(asesorAcademico);
        return ResponseEntity.status(201).body(asesorCreado);
    }

    @GetMapping
    public ResponseEntity<List<AsesorAcademico>> obtenerTodosLosAsesores() {
        return ResponseEntity.ok(asesorAcademicoService.obtenerTodosLosAsesoresAcademicos());
    }

    @GetMapping("/{idAsesor}")
    public ResponseEntity<AsesorAcademico> obtenerAsesorPorId(@PathVariable("idAsesor") Long idAsesor) {
        return asesorAcademicoService.obtenerAsesorPorId(idAsesor)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{idAsesor}")
    public ResponseEntity<AsesorAcademico> actualizarAsesor(
            @PathVariable("idAsesor") Long idAsesor,
            @RequestBody AsesorAcademico asesorActualizado) {

        AsesorAcademico asesor = asesorAcademicoService.actualizarAsesor(
                idAsesor, asesorActualizado);

        if (asesor != null) {
            return ResponseEntity.ok(asesor);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{idAsesor}")
    public ResponseEntity<Void> eliminarAsesor(@PathVariable("idAsesor") Long idAsesor) {
        return asesorAcademicoService.eliminarAsesor(idAsesor)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
