package com.practica.webAvanzada.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.practica.webAvanzada.models.Alumno;
import com.practica.webAvanzada.models.AsesorAcademico;
import com.practica.webAvanzada.repositories.AlumnoRepositorio;
import com.practica.webAvanzada.repositories.AsesorAcademicoRepositorio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlumnoService {

    private final AlumnoRepositorio alumnoRepositorio;
    private final AsesorAcademicoRepositorio asesorRepositorio;
    
    public Alumno crearAlumno(Alumno alumno) {
        return alumnoRepositorio.save(alumno);
    }

    public List<Alumno> obtenerTodosLosAlumnos() {
        return alumnoRepositorio.findAll();
    }

    public List<Alumno> obtenerAlumnosVisibles(Long idUsuario, boolean administrador) {
        return administrador ? alumnoRepositorio.findAll()
                : alumnoRepositorio.findByAsesorAcademico_Usuario_IdUsuario(idUsuario);
    }

    public Optional<Alumno> obtenerAlumnoVisible(String numeroControl, Long idUsuario, boolean administrador) {
        return administrador ? alumnoRepositorio.findByNumeroControl(numeroControl)
                : alumnoRepositorio.findByNumeroControlAndAsesorAcademico_Usuario_IdUsuario(numeroControl, idUsuario);
    }

    public Optional<Alumno> obtenerAlumnoPorUsuario(Long idUsuario) {
        return alumnoRepositorio.findByUsuario_IdUsuario(idUsuario);
    }

    @Transactional
    public Alumno actualizarHabilidades(Long idUsuario, String habilidadesTecnicas) {
        Alumno alumno = alumnoRepositorio.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "La cuenta no tiene perfil de alumno."));
        String habilidades = habilidadesTecnicas == null ? "" : habilidadesTecnicas.trim();
        if (habilidades.length() > 1000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Las habilidades no pueden superar 1000 caracteres.");
        }
        alumno.setHabilidadesTecnicas(habilidades);
        return alumnoRepositorio.save(alumno);
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

    public Alumno actualizarAlumnoVisible(String numeroControl, Alumno datos, Long idUsuario, boolean administrador) {
        return obtenerAlumnoVisible(numeroControl, idUsuario, administrador).map(alumno -> {
            alumno.setNombreCompleto(datos.getNombreCompleto());
            alumno.setCarrera(datos.getCarrera());
            alumno.setSemestre(datos.getSemestre());
            alumno.setHabilidadesTecnicas(datos.getHabilidadesTecnicas());
            alumno.setEstadoResidencia(datos.getEstadoResidencia());
            return alumnoRepositorio.save(alumno);
        }).orElse(null);
    }

    @Transactional
    public Alumno asignarAsesor(String numeroControl, Long idAsesor) {
        Alumno alumno = alumnoRepositorio.findByNumeroControlForUpdate(numeroControl)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe ese alumno."));
        AsesorAcademico asesor = asesorRepositorio.findByIdForUpdate(idAsesor)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe ese asesor."));
        if (alumno.getAsesorAcademico() == null
                || !alumno.getAsesorAcademico().getIdAsesor().equals(asesor.getIdAsesor())) {
            long asignados = alumnoRepositorio.countByAsesorAcademico_IdAsesor(asesor.getIdAsesor());
            if (asignados >= asesor.getMaximoAlumnos()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El asesor alcanzó su máximo de alumnos.");
            }
        }
        alumno.setAsesorAcademico(asesor);
        return alumnoRepositorio.save(alumno);
    }

    public boolean eliminarAlumno(String numeroControl) {
        if (!alumnoRepositorio.existsById(numeroControl)) {
            return false;
        }
        alumnoRepositorio.deleteById(numeroControl);
        return true;
    }
}
