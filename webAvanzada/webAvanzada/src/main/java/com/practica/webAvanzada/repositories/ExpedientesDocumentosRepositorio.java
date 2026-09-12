package com.practica.webAvanzada.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.practica.webAvanzada.models.ExpedientesDocumentos;

public interface ExpedientesDocumentosRepositorio extends JpaRepository<ExpedientesDocumentos, Long> {
}
