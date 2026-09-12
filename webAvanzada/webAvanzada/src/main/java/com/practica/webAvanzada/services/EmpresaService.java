package com.practica.webAvanzada.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import com.practica.webAvanzada.models.Empresa;
import com.practica.webAvanzada.repositories.EmpresaRepositorio;

@Service
@RequiredArgsConstructor
public class EmpresaService {
    private final EmpresaRepositorio empresaRepositorio;

    public Empresa crear(Empresa empresa) {
        return empresaRepositorio.save(empresa);
    }

    public List<Empresa> obtenerTodas() {
        return empresaRepositorio.findAll();
    }

    public Optional<Empresa> obtenerPorId(Long id) {
        return empresaRepositorio.findById(id);
    }

    public Empresa actualizar(Long id, Empresa datos) {
        return empresaRepositorio.findById(id).map(empresa -> {
            empresa.setUsuario(datos.getUsuario());
            empresa.setRazonSocial(datos.getRazonSocial());
            empresa.setSector(datos.getSector());
            empresa.setNombreCompleto(datos.getNombreCompleto());
            empresa.setTelefono(datos.getTelefono());
            empresa.setDireccion(datos.getDireccion());
            empresa.setEstadoVerificacion(datos.getEstadoVerificacion());
            return empresaRepositorio.save(empresa);
        }).orElse(null);
    }

    public boolean eliminar(Long id) {
        if (!empresaRepositorio.existsById(id)) {
            return false;
        }
        empresaRepositorio.deleteById(id);
        return true;
    }
}
