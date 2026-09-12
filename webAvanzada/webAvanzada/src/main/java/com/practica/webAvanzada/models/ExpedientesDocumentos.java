package com.practica.webAvanzada.models;

import java.sql.Date;

import com.practica.webAvanzada.models.enums.EstadoAprobacion;
import com.practica.webAvanzada.models.enums.TipoDocumento;

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
@Table(name = "expedientes_documentos")
public class ExpedientesDocumentos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_documento")
    private Long idDocumento;

    @Column(name = "folio_documento", nullable = false, unique = true)
    private String folioDocumento;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_postulacion", referencedColumnName = "id_postulacion", nullable = false)
    private Postulaciones postulacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false)
    private TipoDocumento tipoDocumento;

    @Column(name = "ruta_archivo_url", nullable = false)
    private String rutaArchivoUrl;

    @Column(name = "fecha_subida", nullable = false)
    private Date fechaSubida;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_aprobacion", nullable = false)
    private EstadoAprobacion estadoAprobacion = EstadoAprobacion.PENDIENTE;
}
