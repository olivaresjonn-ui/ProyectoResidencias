package com.practica.webAvanzada.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.practica.webAvanzada.models.dto.AnaliticaResumen;
import com.practica.webAvanzada.services.AnaliticaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/analitica")
@RequiredArgsConstructor
public class AnaliticaController {

    private final AnaliticaService analiticaService;

    @GetMapping
    public ResponseEntity<AnaliticaResumen> obtenerResumen() {
        return ResponseEntity.ok(analiticaService.obtenerResumen());
    }
}
