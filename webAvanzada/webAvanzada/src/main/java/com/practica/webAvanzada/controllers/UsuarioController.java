package com.practica.webAvanzada.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.practica.webAvanzada.models.Usuario;
import com.practica.webAvanzada.models.dto.CrearUsuarioRequest;
import com.practica.webAvanzada.models.dto.CrearCuentaAlumnoRequest;
import com.practica.webAvanzada.models.dto.CrearCuentaEmpresaRequest;
import com.practica.webAvanzada.models.dto.CrearCuentaAsesorRequest;
import com.practica.webAvanzada.models.dto.ActualizarUsuarioRequest;
import com.practica.webAvanzada.services.UsuarioService;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
    
    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<Usuario> crearUsuario(@Valid @RequestBody CrearUsuarioRequest solicitud) {
        Usuario nuevoUsuario = usuarioService.guardar(solicitud);
        return ResponseEntity.status(201).body(nuevoUsuario);
    }

    @PostMapping("/alumnos")
    public ResponseEntity<Usuario> crearCuentaAlumno(@Valid @RequestBody CrearCuentaAlumnoRequest solicitud) {
        Usuario nuevoUsuario = usuarioService.crearCuentaAlumno(solicitud);
        return ResponseEntity.status(201).body(nuevoUsuario);
    }

    @PostMapping("/empresas")
    public ResponseEntity<Usuario> crearCuentaEmpresa(@Valid @RequestBody CrearCuentaEmpresaRequest solicitud) {
        Usuario nuevoUsuario = usuarioService.crearCuentaEmpresa(solicitud);
        return ResponseEntity.status(201).body(nuevoUsuario);
    }

    @PostMapping("/asesores")
    public ResponseEntity<Usuario> crearCuentaAsesor(@Valid @RequestBody CrearCuentaAsesorRequest solicitud) {
        Usuario nuevoUsuario = usuarioService.crearCuentaAsesor(solicitud);
        return ResponseEntity.status(201).body(nuevoUsuario);
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> obtenerTodosLosUsuarios() {
        return ResponseEntity.ok(usuarioService.obtenerTodosLosUsuarios());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerUsuarioPorId(@PathVariable("id") Long id) {
        return usuarioService.obtenerUsuarioPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable("id") Long id) {
        return usuarioService.eliminarUsuario(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizarUsuario(
            @PathVariable("id") Long id,
            @Valid @RequestBody ActualizarUsuarioRequest usuarioActualizado) {
        Usuario usuario = usuarioService.actualizarUsuario(id, usuarioActualizado);
        if (usuario != null) {
            return ResponseEntity.ok(usuario);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
