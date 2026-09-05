package com.practica.webAvanzada.models;

import com.practica.webAvanzada.models.enums.EstadoVerificacion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "empresas")

public class Empresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empresa")
    private Long idEmpresa;

    @OneToOne(optional = false)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario", nullable = false, unique = true)
    private Usuario usuario;
    
    @Column(name = "razon_social", nullable = false)
    private String razonSocial;

    @Column(nullable = false)
    private String sector;

    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;

    @Column(nullable = false)
    private String telefono;

    @Column(nullable = false)
    private String direccion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_verificacion", nullable = false)
    private EstadoVerificacion estadoVerificacion = EstadoVerificacion.PENDIENTE;
}
