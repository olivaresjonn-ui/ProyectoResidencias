package com.practica.webAvanzada.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import com.practica.webAvanzada.models.Alumno;
import com.practica.webAvanzada.models.dto.AsignarAsesorRequest;
import com.practica.webAvanzada.models.dto.ActualizarHabilidadesRequest;
import com.practica.webAvanzada.services.AlumnoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/alumnos")
@RequiredArgsConstructor
public class AlumnoController {

    private final AlumnoService alumnoService;

    @GetMapping("/mio")
    public ResponseEntity<Alumno> obtenerMiPerfil(@AuthenticationPrincipal Jwt jwt) {
        Number idUsuario = jwt.getClaim("idUsuario");
        return alumnoService.obtenerAlumnoPorUsuario(idUsuario.longValue())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/mio/habilidades")
    public ResponseEntity<Alumno> actualizarMisHabilidades(
            @Valid @RequestBody ActualizarHabilidadesRequest solicitud,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(alumnoService.actualizarHabilidades(idUsuario(jwt), solicitud.habilidadesTecnicas()));
    }

    @PostMapping
    public ResponseEntity<Alumno> crearAlumno(@RequestBody Alumno alumno) {
        Alumno alumnoCreado = alumnoService.crearAlumno(alumno);
        return ResponseEntity.status(201).body(alumnoCreado);
    }

    @GetMapping
    public ResponseEntity<List<Alumno>> obtenerTodosLosAlumnos(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(alumnoService.obtenerAlumnosVisibles(idUsuario(jwt), tieneRol(jwt, "ADMIN")));
    }

    @GetMapping("/{numeroControl}")
    public ResponseEntity<Alumno> obtenerAlumnoPorNumeroControl(
            @PathVariable("numeroControl") String numeroControl, @AuthenticationPrincipal Jwt jwt) {
        return alumnoService.obtenerAlumnoVisible(numeroControl, idUsuario(jwt), tieneRol(jwt, "ADMIN"))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{numeroControl}")
    public ResponseEntity<Alumno> actualizarAlumno(
            @PathVariable("numeroControl") String numeroControl,
            @RequestBody Alumno alumnoActualizado,
            @AuthenticationPrincipal Jwt jwt) {

        Alumno alumno = alumnoService.actualizarAlumnoVisible(
                numeroControl, alumnoActualizado, idUsuario(jwt), tieneRol(jwt, "ADMIN"));

        if (alumno != null) {
            return ResponseEntity.ok(alumno);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{numeroControl}/asesor")
    public ResponseEntity<Alumno> asignarAsesor(
            @PathVariable("numeroControl") String numeroControl,
            @Valid @RequestBody AsignarAsesorRequest solicitud) {
        return ResponseEntity.ok(alumnoService.asignarAsesor(numeroControl, solicitud.idAsesor()));
    }

    @DeleteMapping("/{numeroControl}")
    public ResponseEntity<Void> eliminarAlumno(@PathVariable("numeroControl") String numeroControl) {

        return alumnoService.eliminarAlumno(numeroControl)
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
