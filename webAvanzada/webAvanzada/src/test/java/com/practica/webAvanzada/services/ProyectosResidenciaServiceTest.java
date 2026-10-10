package com.practica.webAvanzada.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.practica.webAvanzada.models.enums.EstadoProyecto;

class ProyectosResidenciaServiceTest {

    @Test
    void alumnoPuedeConsultarProyectoDisponible() {
        assertTrue(ProyectosResidenciaService.alumnoPuedeConsultar(EstadoProyecto.DISPONIBLE));
    }

    @Test
    void alumnoNoPuedeConsultarProyectoAsignadoNiCerrado() {
        assertFalse(ProyectosResidenciaService.alumnoPuedeConsultar(EstadoProyecto.ASIGNADO));
        assertFalse(ProyectosResidenciaService.alumnoPuedeConsultar(EstadoProyecto.CERRADO));
    }
}
