package com.practica.webAvanzada.services;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.practica.webAvanzada.models.Empresa;
import com.practica.webAvanzada.models.ProyectosResidencia;
import com.practica.webAvanzada.models.dto.ActualizarProyectoRequest;
import com.practica.webAvanzada.models.dto.CrearProyectoRequest;
import com.practica.webAvanzada.models.dto.EmpresaProyectoResumen;
import com.practica.webAvanzada.models.dto.ProyectoResumen;
import com.practica.webAvanzada.models.enums.EstadoProyecto;
import com.practica.webAvanzada.models.enums.EstadoVerificacion;
import com.practica.webAvanzada.repositories.EmpresaRepositorio;
import com.practica.webAvanzada.repositories.PostulacionesRepositorio;
import com.practica.webAvanzada.repositories.ProyectosResidenciaRepositorio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProyectosResidenciaService {

    private final ProyectosResidenciaRepositorio proyectosRepositorio;
    private final EmpresaRepositorio empresaRepositorio;
    private final PostulacionesRepositorio postulacionesRepositorio;

    @Transactional
    public ProyectoResumen crear(CrearProyectoRequest datos, Long idUsuario, boolean administrador) {
        String codigoProyecto = textoValido(datos.codigoProyecto(), 3, 40, "código del proyecto");
        if (proyectosRepositorio.existsByCodigoProyecto(codigoProyecto)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese código de proyecto ya está registrado.");
        }

        Empresa empresa;
        if (administrador) {
            if (datos.idEmpresa() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El administrador debe indicar la empresa propietaria.");
            }
            empresa = empresaRepositorio.findById(datos.idEmpresa())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe esa empresa."));
        } else {
            empresa = empresaRepositorio.findByUsuario_IdUsuario(idUsuario)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "La cuenta no tiene perfil de empresa."));
            if (empresa.getEstadoVerificacion() != EstadoVerificacion.VERIFICADA) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La empresa debe estar verificada antes de publicar proyectos.");
            }
        }

        ProyectosResidencia proyecto = new ProyectosResidencia();
        proyecto.setCodigoProyecto(codigoProyecto);
        proyecto.setEmpresa(empresa);
        proyecto.setTitulo(textoValido(datos.titulo(), 5, 160, "título"));
        proyecto.setDescripcion(textoValido(datos.descripcion(), 20, 5000, "descripción"));
        proyecto.setObjetivo(textoValido(datos.objetivo(), 10, 5000, "objetivo"));
        proyecto.setTecnologiasRequeridas(textoValido(datos.tecnologiasRequeridas(), 2, 1000, "tecnologías"));
        proyecto.setCupoAlumnos(datos.cupoAlumnos());
        proyecto.setEstado(EstadoProyecto.DISPONIBLE);
        proyecto.setFechaPublicacion(Date.valueOf(LocalDate.now()));
        return resumir(proyectosRepositorio.save(proyecto));
    }

    @Transactional(readOnly = true)
    public List<ProyectoResumen> obtenerVisibles(Long idUsuario, boolean empresa, boolean administradorOAsesor) {
        List<ProyectosResidencia> proyectos;
        if (administradorOAsesor) {
            proyectos = proyectosRepositorio.findAll();
        } else if (empresa) {
            proyectos = proyectosRepositorio.findByEmpresa_Usuario_IdUsuario(idUsuario);
        } else {
            proyectos = proyectosRepositorio.findByEstado(EstadoProyecto.DISPONIBLE);
        }
        return proyectos.stream().map(this::resumir).toList();
    }

    @Transactional(readOnly = true)
    public List<ProyectoResumen> obtenerMios(Long idUsuario) {
        return proyectosRepositorio.findByEmpresa_Usuario_IdUsuario(idUsuario).stream().map(this::resumir).toList();
    }

    @Transactional(readOnly = true)
    public Optional<ProyectoResumen> obtenerPorId(Long id, Long idUsuario, boolean empresa, boolean administradorOAsesor) {
        Optional<ProyectosResidencia> proyecto = empresa
                ? proyectosRepositorio.findByIdProyectoAndEmpresa_Usuario_IdUsuario(id, idUsuario)
                : proyectosRepositorio.findById(id);
        if (!empresa && !administradorOAsesor) {
            proyecto = proyecto.filter(value -> alumnoPuedeConsultar(value.getEstado()));
        }
        return proyecto.map(this::resumir);
    }

    static boolean alumnoPuedeConsultar(EstadoProyecto estado) {
        return estado == EstadoProyecto.DISPONIBLE;
    }

    @Transactional
    public ProyectoResumen actualizar(Long id, ActualizarProyectoRequest datos, Long idUsuario, boolean administrador) {
        Optional<ProyectosResidencia> encontrado = proyectosRepositorio.findByIdForUpdate(id);
        if (encontrado.isEmpty()) return null;
        ProyectosResidencia proyecto = encontrado.get();
        if (!administrador && !proyecto.getEmpresa().getUsuario().getIdUsuario().equals(idUsuario)) return null;

        long aceptadas = postulacionesRepositorio.countByProyectoResidencia_IdProyectoAndEstadoPostulacion(
                id, com.practica.webAvanzada.models.enums.EstadoPostulacion.ACEPTADA);
        if (datos.cupoAlumnos() < aceptadas) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El cupo no puede ser menor al número de postulaciones aceptadas.");
        }
        if (datos.estado() == EstadoProyecto.ASIGNADO && aceptadas < datos.cupoAlumnos()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El estado ASIGNADO se establece al completar todos los cupos.");
        }

        String titulo = textoValido(datos.titulo(), 5, 160, "título");
        String descripcion = textoValido(datos.descripcion(), 20, 5000, "descripción");
        String objetivo = textoValido(datos.objetivo(), 10, 5000, "objetivo");
        String tecnologias = textoValido(datos.tecnologiasRequeridas(), 2, 1000, "tecnologías");

        proyecto.setTitulo(titulo);
        proyecto.setDescripcion(descripcion);
        proyecto.setObjetivo(objetivo);
        proyecto.setTecnologiasRequeridas(tecnologias);
        proyecto.setCupoAlumnos(datos.cupoAlumnos());
        proyecto.setEstado(aceptadas >= datos.cupoAlumnos() && datos.estado() != EstadoProyecto.CERRADO
                ? EstadoProyecto.ASIGNADO
                : datos.estado());
        return resumir(proyectosRepositorio.save(proyecto));
    }

    @Transactional
    public boolean eliminar(Long id, Long idUsuario, boolean administrador) {
        Optional<ProyectosResidencia> encontrado = administrador
                ? proyectosRepositorio.findById(id)
                : proyectosRepositorio.findByIdProyectoAndEmpresa_Usuario_IdUsuario(id, idUsuario);
        if (encontrado.isEmpty()) return false;
        if (postulacionesRepositorio.existsByProyectoResidencia_IdProyecto(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar un proyecto con postulaciones; puedes cerrarlo para detener nuevas solicitudes.");
        }
        proyectosRepositorio.delete(encontrado.get());
        return true;
    }

    private ProyectoResumen resumir(ProyectosResidencia proyecto) {
        Empresa empresa = proyecto.getEmpresa();
        EmpresaProyectoResumen empresaResumen = new EmpresaProyectoResumen(
                empresa.getIdEmpresa(), empresa.getRazonSocial(), empresa.getSector());
        return new ProyectoResumen(proyecto.getIdProyecto(), proyecto.getCodigoProyecto(), proyecto.getTitulo(),
                proyecto.getDescripcion(), proyecto.getObjetivo(), proyecto.getTecnologiasRequeridas(),
                proyecto.getCupoAlumnos(), proyecto.getEstado(), proyecto.getFechaPublicacion(), empresaResumen);
    }

    private String textoValido(String valor, int minimo, int maximo, String campo) {
        String texto = valor == null ? "" : valor.trim();
        if (texto.length() < minimo || texto.length() > maximo) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El campo " + campo + " debe tener entre " + minimo + " y " + maximo + " caracteres.");
        }
        return texto;
    }
}
