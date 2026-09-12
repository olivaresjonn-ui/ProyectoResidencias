package com.practica.webAvanzada.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.practica.webAvanzada.models.ExpedientesDocumentos;
import com.practica.webAvanzada.repositories.ExpedientesDocumentosRepositorio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExpedientesDocumentosService {

    private final ExpedientesDocumentosRepositorio expedientesDocumentosRepositorio;

    public ExpedientesDocumentos crear(ExpedientesDocumentos documento) {
        return expedientesDocumentosRepositorio.save(documento);
    }

    public List<ExpedientesDocumentos> obtenerTodos() {
        return expedientesDocumentosRepositorio.findAll();
    }

    public Optional<ExpedientesDocumentos> obtenerPorId(Long id) {
        return expedientesDocumentosRepositorio.findById(id);
    }

    public ExpedientesDocumentos actualizar(Long id, ExpedientesDocumentos datos) {
        return expedientesDocumentosRepositorio.findById(id).map(documento -> {
            documento.setPostulacion(datos.getPostulacion());
            documento.setTipoDocumento(datos.getTipoDocumento());
            documento.setRutaArchivoUrl(datos.getRutaArchivoUrl());
            documento.setFechaSubida(datos.getFechaSubida());
            documento.setEstadoAprobacion(datos.getEstadoAprobacion());
            return expedientesDocumentosRepositorio.save(documento);
        }).orElse(null);
    }

    public boolean eliminar(Long id) {
        if (!expedientesDocumentosRepositorio.existsById(id)) {
            return false;
        }
        expedientesDocumentosRepositorio.deleteById(id);
        return true;
    }
}
