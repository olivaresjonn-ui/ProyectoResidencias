package com.practica.webAvanzada.models.dto;

import java.sql.Date;

import com.practica.webAvanzada.models.enums.EstadoAprobacion;
import com.practica.webAvanzada.models.enums.TipoDocumento;

public record DocumentoResumen(
        Long idDocumento,
        String folioDocumento,
        Long idPostulacion,
        TipoDocumento tipoDocumento,
        Date fechaSubida,
        EstadoAprobacion estadoAprobacion) {
}
