package com.practica.webAvanzada.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.practica.webAvanzada.models.ExpedientesDocumentos;

public interface ExpedientesDocumentosRepositorio extends JpaRepository<ExpedientesDocumentos, Long> {
    java.util.List<ExpedientesDocumentos> findByPostulacion_Alumno_Usuario_IdUsuario(Long idUsuario);

    java.util.List<ExpedientesDocumentos> findByPostulacion_Alumno_AsesorAcademico_Usuario_IdUsuario(Long idUsuario);

    boolean existsByPostulacion_IdPostulacion(Long idPostulacion);
}
