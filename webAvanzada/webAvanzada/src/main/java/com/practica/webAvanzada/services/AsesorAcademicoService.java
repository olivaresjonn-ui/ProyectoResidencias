package com.practica.webAvanzada.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.practica.webAvanzada.models.AsesorAcademico;
import com.practica.webAvanzada.repositories.AsesorAcademicoRepositorio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AsesorAcademicoService {
    private final AsesorAcademicoRepositorio asesorAcademicoRepositorio;

    public AsesorAcademico crearAsesorAcademico(AsesorAcademico asesorAcademico) {
        return asesorAcademicoRepositorio.save(asesorAcademico);
    }

    public List<AsesorAcademico> obtenerTodosLosAsesoresAcademicos() {
        return asesorAcademicoRepositorio.findAll();
    }

    public Optional<AsesorAcademico> obtenerAsesorPorId(Long idAsesor) {
        return asesorAcademicoRepositorio.findById(idAsesor);
    }

    public Optional<AsesorAcademico> obtenerAsesorPorUsuario(Long idUsuario) {
        return asesorAcademicoRepositorio.findByUsuario_IdUsuario(idUsuario);
    }

    public AsesorAcademico actualizarAsesor(Long idAsesor, AsesorAcademico asesorActualizado) {

    Optional<AsesorAcademico> asesorExistenteOptional =
            asesorAcademicoRepositorio.findById(idAsesor);

    if (asesorExistenteOptional.isPresent()) {

        AsesorAcademico asesorExistente = asesorExistenteOptional.get();

        asesorExistente.setNombreCompleto(asesorActualizado.getNombreCompleto());
        asesorExistente.setDepartamento(asesorActualizado.getDepartamento());
        asesorExistente.setMaximoAlumnos(asesorActualizado.getMaximoAlumnos());

        return asesorAcademicoRepositorio.save(asesorExistente);

    } else {
        return null;
    }
}
    public boolean eliminarAsesor(Long idAsesor) {
        if (!asesorAcademicoRepositorio.existsById(idAsesor)) {
            return false;
        }
        asesorAcademicoRepositorio.deleteById(idAsesor);
        return true;
    }
}
