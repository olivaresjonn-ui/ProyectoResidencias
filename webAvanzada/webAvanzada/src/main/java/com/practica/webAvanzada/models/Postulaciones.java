package com.practica.webAvanzada.models;

import java.sql.Date;

import com.practica.webAvanzada.models.enums.EstadoPostulacion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "postulaciones")
public class Postulaciones {
    @Id
    @Column(name = "id_postulacion")
    private String idPostulacion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "numero_control", referencedColumnName = "numero_control", nullable = false)
    private Alumno alumno;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_proyecto", referencedColumnName = "id_proyecto", nullable = false)
    private ProyectosResidencia proyectoResidencia;

    @Column(name = "fecha_solicitud", nullable = false)
    private Date fechaSolicitud;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_postulacion", nullable = false)
    private EstadoPostulacion estadoPostulacion = EstadoPostulacion.EN_REVISION;

    @Column(name = "comentarios", columnDefinition = "TEXT")
    private String comentarios;
}
