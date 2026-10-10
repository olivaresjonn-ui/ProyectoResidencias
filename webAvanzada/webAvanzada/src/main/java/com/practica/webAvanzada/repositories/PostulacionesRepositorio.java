package com.practica.webAvanzada.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import com.practica.webAvanzada.models.Postulaciones;

public interface PostulacionesRepositorio extends JpaRepository<Postulaciones, Long> {
    java.util.List<Postulaciones> findByAlumno_Usuario_IdUsuario(Long idUsuario);

    java.util.List<Postulaciones> findByAlumno_AsesorAcademico_Usuario_IdUsuario(Long idUsuario);

    boolean existsByAlumno_NumeroControlAndProyectoResidencia_IdProyecto(String numeroControl, Long idProyecto);

    boolean existsByProyectoResidencia_IdProyecto(Long idProyecto);

    java.util.Optional<Postulaciones> findByIdPostulacionAndAlumno_AsesorAcademico_Usuario_IdUsuario(Long idPostulacion, Long idUsuario);

    long countByProyectoResidencia_IdProyectoAndEstadoPostulacion(
            Long idProyecto, com.practica.webAvanzada.models.enums.EstadoPostulacion estadoPostulacion);

    long countByEstadoPostulacion(com.practica.webAvanzada.models.enums.EstadoPostulacion estadoPostulacion);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Postulaciones p where p.idPostulacion = :idPostulacion")
    java.util.Optional<Postulaciones> findByIdForUpdate(@Param("idPostulacion") Long idPostulacion);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Postulaciones p where p.idPostulacion = :idPostulacion and p.alumno.asesorAcademico.usuario.idUsuario = :idUsuario")
    java.util.Optional<Postulaciones> findByIdAndAsesorForUpdate(
            @Param("idPostulacion") Long idPostulacion, @Param("idUsuario") Long idUsuario);
}
