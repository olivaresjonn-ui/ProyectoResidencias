package com.practica.webAvanzada.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.practica.webAvanzada.models.Alumno;
import com.practica.webAvanzada.repositories.AlumnoRepositorio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlumnoService {

    private final AlumnoRepositorio alumnoRepositorio;
    
    public Alumno crearAlumno(Alumno alumno) {
        return alumnoRepositorio.save(alumno);
    }

    public List<Alumno> obtenerTodosLosAlumnos() {
        return alumnoRepositorio.findAll();
    }

    public Optional<Alumno> obtenerAlumnoPorNumeroControl(String numeroControl) {
        return alumnoRepositorio.findByNumeroControl(numeroControl);
    }

    public Alumno actualizarAlumno(String numeroControl, Alumno alumnoActualizado) {
        Optional<Alumno> alumnoExistenteOptional = alumnoRepositorio.findByNumeroControl(numeroControl);

        if (alumnoExistenteOptional.isPresent()) {
            Alumno alumnoExistente = alumnoExistenteOptional.get();

            alumnoExistente.setNombreCompleto(alumnoActualizado.getNombreCompleto());
            alumnoExistente.setCarrera(alumnoActualizado.getCarrera());
            alumnoExistente.setSemestre(alumnoActualizado.getSemestre());
            alumnoExistente.setHabilidadesTecnicas(alumnoActualizado.getHabilidadesTecnicas());
            alumnoExistente.setEstadoResidencia(alumnoActualizado.getEstadoResidencia());

            return alumnoRepositorio.save(alumnoExistente);
        } else {
            return null;
        }
    }

    public boolean eliminarAlumno(String numeroControl) {
        if (!alumnoRepositorio.existsById(numeroControl)) {
            return false;
        }
        alumnoRepositorio.deleteById(numeroControl);
        return true;
    }
}
