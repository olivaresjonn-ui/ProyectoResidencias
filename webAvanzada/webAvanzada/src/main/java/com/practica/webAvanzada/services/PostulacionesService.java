package com.practica.webAvanzada.services;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.practica.webAvanzada.models.Alumno;
import com.practica.webAvanzada.models.Postulaciones;
import com.practica.webAvanzada.models.ProyectosResidencia;
import com.practica.webAvanzada.models.dto.ActualizarPostulacionRequest;
import com.practica.webAvanzada.models.dto.CrearPostulacionRequest;
import com.practica.webAvanzada.models.dto.PostulacionResumen;
import com.practica.webAvanzada.models.enums.EstadoPostulacion;
import com.practica.webAvanzada.models.enums.EstadoProyecto;
import com.practica.webAvanzada.repositories.AlumnoRepositorio;
import com.practica.webAvanzada.repositories.ExpedientesDocumentosRepositorio;
import com.practica.webAvanzada.repositories.PostulacionesRepositorio;
import com.practica.webAvanzada.repositories.ProyectosResidenciaRepositorio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostulacionesService {

    private final PostulacionesRepositorio postulacionesRepositorio;
    private final AlumnoRepositorio alumnoRepositorio;
    private final ProyectosResidenciaRepositorio proyectosRepositorio;
    private final ExpedientesDocumentosRepositorio documentosRepositorio;

    @Transactional
    public PostulacionResumen crear(CrearPostulacionRequest solicitud, Long idUsuario) {
        Alumno alumno = alumnoRepositorio.findByUsuario_IdUsuario(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "La cuenta no tiene perfil de alumno."));
        ProyectosResidencia proyecto = proyectosRepositorio.findByIdForUpdate(solicitud.idProyecto())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe ese proyecto."));

        if (proyecto.getEstado() != EstadoProyecto.DISPONIBLE || proyecto.getCupoAlumnos() == null
                || proyecto.getCupoAlumnos() < 1
                || postulacionesRepositorio.countByProyectoResidencia_IdProyectoAndEstadoPostulacion(
                        proyecto.getIdProyecto(), EstadoPostulacion.ACEPTADA) >= proyecto.getCupoAlumnos()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El proyecto ya no tiene lugares disponibles.");
        }
        if (postulacionesRepositorio.existsByAlumno_NumeroControlAndProyectoResidencia_IdProyecto(
                alumno.getNumeroControl(), proyecto.getIdProyecto())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya tienes una postulación para este proyecto.");
        }

        Postulaciones postulacion = new Postulaciones();
        postulacion.setFolioPostulacion("POS-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        postulacion.setAlumno(alumno);
        postulacion.setProyectoResidencia(proyecto);
        postulacion.setFechaSolicitud(java.sql.Date.valueOf(LocalDate.now()));
        postulacion.setEstadoPostulacion(EstadoPostulacion.EN_REVISION);
        return resumir(postulacionesRepositorio.save(postulacion));
    }

    @Transactional(readOnly = true)
    public List<PostulacionResumen> obtenerTodas(Long idUsuario, boolean administrador) {
        List<Postulaciones> postulaciones = administrador ? postulacionesRepositorio.findAll()
                : postulacionesRepositorio.findByAlumno_AsesorAcademico_Usuario_IdUsuario(idUsuario);
        return postulaciones.stream().map(this::resumir).toList();
    }

    @Transactional(readOnly = true)
    public List<PostulacionResumen> obtenerMias(Long idUsuario) {
        return postulacionesRepositorio.findByAlumno_Usuario_IdUsuario(idUsuario).stream().map(this::resumir).toList();
    }

    @Transactional(readOnly = true)
    public Optional<PostulacionResumen> obtenerPorId(Long id, Long idUsuario, boolean administrador) {
        Optional<Postulaciones> postulacion = administrador ? postulacionesRepositorio.findById(id)
                : postulacionesRepositorio.findByIdPostulacionAndAlumno_AsesorAcademico_Usuario_IdUsuario(id, idUsuario);
        return postulacion.map(this::resumir);
    }

    @Transactional
    public PostulacionResumen actualizar(Long id, ActualizarPostulacionRequest datos, Long idUsuario, boolean administrador) {
        Optional<Postulaciones> encontrada = administrador
                ? postulacionesRepositorio.findByIdForUpdate(id)
                : postulacionesRepositorio.findByIdAndAsesorForUpdate(id, idUsuario);
        if (encontrada.isEmpty()) return null;

        Postulaciones postulacion = encontrada.get();
        EstadoPostulacion anterior = postulacion.getEstadoPostulacion();
        EstadoPostulacion nuevo = datos.estadoPostulacion();
        if (anterior == EstadoPostulacion.ACEPTADA && nuevo == EstadoPostulacion.EN_REVISION) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Una postulación aceptada no puede regresar a revisión.");
        }

        ProyectosResidencia proyecto = proyectosRepositorio.findByIdForUpdate(
                postulacion.getProyectoResidencia().getIdProyecto()).orElseThrow();
        long aceptadas = postulacionesRepositorio.countByProyectoResidencia_IdProyectoAndEstadoPostulacion(
                proyecto.getIdProyecto(), EstadoPostulacion.ACEPTADA);

        if (nuevo == EstadoPostulacion.ACEPTADA && anterior != EstadoPostulacion.ACEPTADA) {
            if (proyecto.getEstado() == EstadoProyecto.CERRADO || proyecto.getEstado() == EstadoProyecto.ASIGNADO
                    || proyecto.getCupoAlumnos() == null || aceptadas >= proyecto.getCupoAlumnos()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El proyecto ya no tiene cupos disponibles para aceptar más residentes.");
            }
            aceptadas++;
        } else if (anterior == EstadoPostulacion.ACEPTADA && nuevo != EstadoPostulacion.ACEPTADA) {
            aceptadas--;
        }

        postulacion.setEstadoPostulacion(nuevo);
        postulacion.setComentarios(datos.comentarios());
        if (proyecto.getEstado() != EstadoProyecto.CERRADO && proyecto.getCupoAlumnos() != null) {
            proyecto.setEstado(aceptadas >= proyecto.getCupoAlumnos()
                    ? EstadoProyecto.ASIGNADO
                    : EstadoProyecto.DISPONIBLE);
            proyectosRepositorio.save(proyecto);
        }
        postulacionesRepositorio.save(postulacion);
        return resumir(postulacion);
    }

    private PostulacionResumen resumir(Postulaciones postulacion) {
        ProyectosResidencia proyecto = postulacion.getProyectoResidencia();
        return new PostulacionResumen(postulacion.getIdPostulacion(), postulacion.getFolioPostulacion(),
                postulacion.getAlumno().getNombreCompleto(), postulacion.getAlumno().getCarrera(),
                proyecto.getIdProyecto(), proyecto.getTitulo(), proyecto.getEmpresa().getRazonSocial(),
                postulacion.getFechaSolicitud(), postulacion.getEstadoPostulacion(), postulacion.getComentarios());
    }

    @Transactional
    public boolean eliminar(Long id) {
        Optional<Postulaciones> encontrada = postulacionesRepositorio.findByIdForUpdate(id);
        if (encontrada.isEmpty()) return false;
        Postulaciones postulacion = encontrada.get();
        if (documentosRepositorio.existsByPostulacion_IdPostulacion(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar una postulación que ya tiene documentos en su expediente.");
        }
        ProyectosResidencia proyecto = proyectosRepositorio.findByIdForUpdate(
                postulacion.getProyectoResidencia().getIdProyecto()).orElseThrow();
        boolean aceptada = postulacion.getEstadoPostulacion() == EstadoPostulacion.ACEPTADA;
        postulacionesRepositorio.delete(postulacion);
        if (aceptada && proyecto.getEstado() == EstadoProyecto.ASIGNADO
                && proyecto.getCupoAlumnos() != null
                && postulacionesRepositorio.countByProyectoResidencia_IdProyectoAndEstadoPostulacion(
                        proyecto.getIdProyecto(), EstadoPostulacion.ACEPTADA) < proyecto.getCupoAlumnos()) {
            proyecto.setEstado(EstadoProyecto.DISPONIBLE);
            proyectosRepositorio.save(proyecto);
        }
        return true;
    }
}
