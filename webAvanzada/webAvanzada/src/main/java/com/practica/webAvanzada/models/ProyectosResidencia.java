package com.practica.webAvanzada.models;

import java.sql.Date;

import com.practica.webAvanzada.models.enums.EstadoProyecto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "proyectos_residencia")
public class ProyectosResidencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proyecto")
    private Long idProyecto;

    @Column(name = "codigo_proyecto", nullable = false, unique = true)
    private String codigoProyecto;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_empresa", referencedColumnName = "id_empresa", nullable = false)
    private Empresa empresa;

    @Column(name = "titulo", nullable = false)
    private String titulo;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "objetivo", nullable = false, columnDefinition = "TEXT")
    private String objetivo;

    @Column(name = "tecnologias_requeridas", nullable = false, columnDefinition = "TEXT")
    private String tecnologiasRequeridas;

    @Column(name = "cupo_alumnos", nullable = false)
    private Integer cupoAlumnos;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoProyecto estado = EstadoProyecto.DISPONIBLE;

    @Column(name = "fecha_publicacion", nullable = false)
    private Date fechaPublicacion;
}
