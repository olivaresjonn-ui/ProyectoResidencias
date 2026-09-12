package com.practica.webAvanzada.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.practica.webAvanzada.models.Empresa;

@Repository
public interface EmpresaRepositorio extends JpaRepository<Empresa, Long> {

}
