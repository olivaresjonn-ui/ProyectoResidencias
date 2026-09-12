package com.practica.webAvanzada.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.practica.webAvanzada.models.Alumno;

@Repository
public interface AlumnoRepositorio extends JpaRepository<Alumno, String> {
    Optional<Alumno> findByNumeroControl(String numeroControl);
}
