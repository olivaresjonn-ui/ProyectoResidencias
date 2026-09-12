package com.practica.webAvanzada.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.practica.webAvanzada.models.ProyectosResidencia;
import com.practica.webAvanzada.repositories.ProyectosResidenciaRepositorio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProyectosResidenciaService {

    private final ProyectosResidenciaRepositorio proyectosResidenciaRepositorio;

    public ProyectosResidencia crear(ProyectosResidencia proyecto) {
        return proyectosResidenciaRepositorio.save(proyecto);
    }

    public List<ProyectosResidencia> obtenerTodos() {
        return proyectosResidenciaRepositorio.findAll();
    }

    public Optional<ProyectosResidencia> obtenerPorId(Long id) {
        return proyectosResidenciaRepositorio.findById(id);
    }

    public ProyectosResidencia actualizar(Long id, ProyectosResidencia datos) {
        return proyectosResidenciaRepositorio.findById(id).map(proyecto -> {
            proyecto.setEmpresa(datos.getEmpresa());
            proyecto.setTitulo(datos.getTitulo());
            proyecto.setDescripcion(datos.getDescripcion());
            proyecto.setObjetivo(datos.getObjetivo());
            proyecto.setTecnologiasRequeridas(datos.getTecnologiasRequeridas());
            proyecto.setCupoAlumnos(datos.getCupoAlumnos());
            proyecto.setEstado(datos.getEstado());
            proyecto.setFechaPublicacion(datos.getFechaPublicacion());
            return proyectosResidenciaRepositorio.save(proyecto);
        }).orElse(null);
    }

    public boolean eliminar(Long id) {
        if (!proyectosResidenciaRepositorio.existsById(id)) {
            return false;
        }
        proyectosResidenciaRepositorio.deleteById(id);
        return true;
    }
}
