package com.practica.webAvanzada.models.dto;

import java.sql.Date;

import com.practica.webAvanzada.models.enums.EstadoProyecto;

public record ProyectoResumen(
        Long idProyecto,
        String codigoProyecto,
        String titulo,
        String descripcion,
        String objetivo,
        String tecnologiasRequeridas,
        Integer cupoAlumnos,
        EstadoProyecto estado,
        Date fechaPublicacion,
        EmpresaProyectoResumen empresa) {
}
