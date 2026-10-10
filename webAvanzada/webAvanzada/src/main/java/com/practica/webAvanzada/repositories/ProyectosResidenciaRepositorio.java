package com.practica.webAvanzada.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import com.practica.webAvanzada.models.ProyectosResidencia;
import com.practica.webAvanzada.models.enums.EstadoProyecto;

import java.util.List;

public interface ProyectosResidenciaRepositorio extends JpaRepository<ProyectosResidencia, Long> {
    List<ProyectosResidencia> findByEmpresa_Usuario_IdUsuario(Long idUsuario);

    List<ProyectosResidencia> findByEstado(EstadoProyecto estado);

    java.util.Optional<ProyectosResidencia> findByIdProyectoAndEmpresa_Usuario_IdUsuario(Long idProyecto, Long idUsuario);

    boolean existsByCodigoProyecto(String codigoProyecto);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProyectosResidencia p where p.idProyecto = :idProyecto")
    java.util.Optional<ProyectosResidencia> findByIdForUpdate(@Param("idProyecto") Long idProyecto);
}
