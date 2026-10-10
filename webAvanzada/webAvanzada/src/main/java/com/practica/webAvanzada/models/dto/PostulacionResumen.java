package com.practica.webAvanzada.models.dto;

import java.sql.Date;

import com.practica.webAvanzada.models.enums.EstadoPostulacion;

public record PostulacionResumen(
        Long idPostulacion,
        String folioPostulacion,
        String nombreAlumno,
        String carreraAlumno,
        Long idProyecto,
        String tituloProyecto,
        String empresa,
        Date fechaSolicitud,
        EstadoPostulacion estadoPostulacion,
        String comentarios) {
}
