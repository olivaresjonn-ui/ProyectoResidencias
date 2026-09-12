package com.practica.webAvanzada.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.practica.webAvanzada.models.Postulaciones;
import com.practica.webAvanzada.repositories.PostulacionesRepositorio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostulacionesService {

    private final PostulacionesRepositorio postulacionesRepositorio;

    public Postulaciones crear(Postulaciones postulacion) {
        return postulacionesRepositorio.save(postulacion);
    }

    public List<Postulaciones> obtenerTodas() {
        return postulacionesRepositorio.findAll();
    }

    public Optional<Postulaciones> obtenerPorId(Long id) {
        return postulacionesRepositorio.findById(id);
    }

    public Postulaciones actualizar(Long id, Postulaciones datos) {
        return postulacionesRepositorio.findById(id).map(postulacion -> {
            postulacion.setAlumno(datos.getAlumno());
            postulacion.setProyectoResidencia(datos.getProyectoResidencia());
            postulacion.setFechaSolicitud(datos.getFechaSolicitud());
            postulacion.setEstadoPostulacion(datos.getEstadoPostulacion());
            postulacion.setComentarios(datos.getComentarios());
            return postulacionesRepositorio.save(postulacion);
        }).orElse(null);
    }

    public boolean eliminar(Long id) {
        if (!postulacionesRepositorio.existsById(id)) {
            return false;
        }
        postulacionesRepositorio.deleteById(id);
        return true;
    }
}
