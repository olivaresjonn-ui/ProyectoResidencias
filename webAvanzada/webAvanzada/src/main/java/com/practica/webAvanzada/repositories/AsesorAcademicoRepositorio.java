package com.practica.webAvanzada.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import com.practica.webAvanzada.models.AsesorAcademico;

import java.util.Optional;

public interface AsesorAcademicoRepositorio extends JpaRepository<AsesorAcademico, Long> {
    Optional<AsesorAcademico> findByUsuario_IdUsuario(Long idUsuario);
    boolean existsByUsuario_IdUsuario(Long idUsuario);
    boolean existsByClaveAsesorIgnoreCase(String claveAsesor);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AsesorAcademico a where a.idAsesor = :idAsesor")
    Optional<AsesorAcademico> findByIdForUpdate(@Param("idAsesor") Long idAsesor);

}
