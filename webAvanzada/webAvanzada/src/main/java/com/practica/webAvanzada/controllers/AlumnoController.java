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

import com.practica.webAvanzada.models.Alumno;
import com.practica.webAvanzada.services.AlumnoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/alumnos")
@RequiredArgsConstructor
public class AlumnoController {

    private final AlumnoService alumnoService;

    @PostMapping
    public ResponseEntity<Alumno> crearAlumno(@RequestBody Alumno alumno) {
        Alumno alumnoCreado = alumnoService.crearAlumno(alumno);
        return ResponseEntity.status(201).body(alumnoCreado);
    }

    @GetMapping
    public ResponseEntity<List<Alumno>> obtenerTodosLosAlumnos() {
        return ResponseEntity.ok(alumnoService.obtenerTodosLosAlumnos());
    }

    @GetMapping("/{numeroControl}")
    public ResponseEntity<Alumno> obtenerAlumnoPorNumeroControl(@PathVariable String numeroControl) {

        return alumnoService.obtenerAlumnoPorNumeroControl(numeroControl)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{numeroControl}")
    public ResponseEntity<Alumno> actualizarAlumno(
            @PathVariable String numeroControl,
            @RequestBody Alumno alumnoActualizado) {

        Alumno alumno = alumnoService.actualizarAlumno(
                numeroControl, alumnoActualizado);

        if (alumno != null) {
            return ResponseEntity.ok(alumno);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{numeroControl}")
    public ResponseEntity<Void> eliminarAlumno(@PathVariable String numeroControl) {

        return alumnoService.eliminarAlumno(numeroControl)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
