package com.practica.webAvanzada.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.practica.webAvanzada.models.ProyectosResidencia;
import com.practica.webAvanzada.models.dto.AnaliticaResumen;
import com.practica.webAvanzada.models.dto.TecnologiaDemanda;
import com.practica.webAvanzada.models.enums.EstadoPostulacion;
import com.practica.webAvanzada.models.enums.EstadoProyecto;
import com.practica.webAvanzada.repositories.PostulacionesRepositorio;
import com.practica.webAvanzada.repositories.ProyectosResidenciaRepositorio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnaliticaService {

    private final ProyectosResidenciaRepositorio proyectosRepositorio;
    private final PostulacionesRepositorio postulacionesRepositorio;

    @Transactional(readOnly = true)
    public AnaliticaResumen obtenerResumen() {
        List<ProyectosResidencia> proyectos = proyectosRepositorio.findAll();
        Map<String, String> nombresTecnologia = new HashMap<>();
        Map<String, Long> conteosTecnologia = new HashMap<>();

        for (ProyectosResidencia proyecto : proyectos) {
            Set<String> tecnologiasDelProyecto = new HashSet<>();
            for (String tecnologia : proyecto.getTecnologiasRequeridas().split("[,;|\\n]+")) {
                String nombre = tecnologia.trim();
                if (!nombre.isEmpty()) {
                    String clave = nombre.toLowerCase(Locale.ROOT);
                    nombresTecnologia.putIfAbsent(clave, nombre);
                    tecnologiasDelProyecto.add(clave);
                }
            }
            tecnologiasDelProyecto.forEach(clave -> conteosTecnologia.merge(clave, 1L, Long::sum));
        }

        List<TecnologiaDemanda> tecnologias = conteosTecnologia.entrySet().stream()
                .map(entrada -> new TecnologiaDemanda(nombresTecnologia.get(entrada.getKey()), entrada.getValue()))
                .sorted(Comparator.comparingLong(TecnologiaDemanda::cantidad).reversed()
                        .thenComparing(TecnologiaDemanda::nombre, String.CASE_INSENSITIVE_ORDER))
                .limit(8)
                .toList();

        Map<String, Long> conteosPostulacion = new LinkedHashMap<>();
        long totalPostulaciones = 0;
        for (EstadoPostulacion estado : EstadoPostulacion.values()) {
            long cantidad = postulacionesRepositorio.countByEstadoPostulacion(estado);
            conteosPostulacion.put(estado.name(), cantidad);
            totalPostulaciones += cantidad;
        }

        long vacantesAbiertas = proyectos.stream()
                .filter(proyecto -> proyecto.getEstado() == EstadoProyecto.DISPONIBLE)
                .count();
        return new AnaliticaResumen(proyectos.size(), vacantesAbiertas, totalPostulaciones,
                conteosTecnologia.size(), tecnologias, conteosPostulacion);
    }
}
