package com.practica.webAvanzada.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "asesores_academicos")
public class AsesoresAcademicos {
    @Id
    @Column(name = "id_asesor")
    private String idAsesor;

    @OneToOne(optional = false)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;

    @Column(nullable = false)
    private String departamento;

    @Column(nullable = false)
    private Integer maximoAlumnos;
}
