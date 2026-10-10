package com.practica.webAvanzada.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.practica.webAvanzada.models.Empresa;

import java.util.Optional;

@Repository
public interface EmpresaRepositorio extends JpaRepository<Empresa, Long> {
    Optional<Empresa> findByUsuario_IdUsuario(Long idUsuario);
    boolean existsByUsuario_IdUsuario(Long idUsuario);

}
