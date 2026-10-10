package com.practica.webAvanzada.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import com.practica.webAvanzada.models.Alumno;

@Repository
public interface AlumnoRepositorio extends JpaRepository<Alumno, String> {
    Optional<Alumno> findByNumeroControl(String numeroControl);
    Optional<Alumno> findByUsuario_IdUsuario(Long idUsuario);
    boolean existsByUsuario_IdUsuario(Long idUsuario);
    java.util.List<Alumno> findByAsesorAcademico_Usuario_IdUsuario(Long idUsuario);
    long countByAsesorAcademico_IdAsesor(Long idAsesor);
    Optional<Alumno> findByNumeroControlAndAsesorAcademico_Usuario_IdUsuario(String numeroControl, Long idUsuario);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Alumno a where a.numeroControl = :numeroControl")
    Optional<Alumno> findByNumeroControlForUpdate(@Param("numeroControl") String numeroControl);
}
