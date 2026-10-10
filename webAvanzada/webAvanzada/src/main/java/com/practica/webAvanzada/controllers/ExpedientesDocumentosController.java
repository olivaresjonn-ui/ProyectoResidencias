package com.practica.webAvanzada.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.practica.webAvanzada.models.dto.ActualizarDocumentoEstadoRequest;
import com.practica.webAvanzada.models.dto.DocumentoResumen;
import com.practica.webAvanzada.models.enums.TipoDocumento;
import com.practica.webAvanzada.services.ExpedientesDocumentosService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/documentos")
@RequiredArgsConstructor
public class ExpedientesDocumentosController {

    private final ExpedientesDocumentosService documentosService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentoResumen> crear(
            @RequestParam("idPostulacion") Long idPostulacion,
            @RequestParam("tipoDocumento") TipoDocumento tipoDocumento,
            @RequestParam("archivo") MultipartFile archivo,
            @AuthenticationPrincipal Jwt jwt) {
        Number idUsuario = jwt.getClaim("idUsuario");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentosService.crear(idPostulacion, tipoDocumento, archivo, idUsuario.longValue()));
    }

    @GetMapping
    public ResponseEntity<List<DocumentoResumen>> obtenerTodos(@AuthenticationPrincipal Jwt jwt) {
        Number idUsuario = jwt.getClaim("idUsuario");
        return ResponseEntity.ok(documentosService.obtenerTodos(idUsuario.longValue(),
                tieneRol(jwt, "ADMIN"), tieneRol(jwt, "ASESOR")));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentoResumen> obtenerPorId(@PathVariable("id") Long id, @AuthenticationPrincipal Jwt jwt) {
        Number idUsuario = jwt.getClaim("idUsuario");
        return documentosService.obtenerPorId(id, idUsuario.longValue(), tieneRol(jwt, "ADMIN"))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/archivo")
    public ResponseEntity<Resource> descargar(@PathVariable("id") Long id, @AuthenticationPrincipal Jwt jwt) {
        Number idUsuario = jwt.getClaim("idUsuario");
        Path archivo = documentosService.obtenerArchivo(id, idUsuario.longValue(), tieneRol(jwt, "ADMIN"));
        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(Files.size(archivo))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.attachment().filename("documento-" + id + ".pdf").build().toString())
                    .body(new FileSystemResource(archivo));
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo leer el documento.", exception);
        }
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<DocumentoResumen> actualizarEstado(
            @PathVariable("id") Long id,
            @Valid @RequestBody ActualizarDocumentoEstadoRequest solicitud,
            @AuthenticationPrincipal Jwt jwt) {
        Number idUsuario = jwt.getClaim("idUsuario");
        return ResponseEntity.ok(documentosService.actualizarEstado(
                id, solicitud.estadoAprobacion(), idUsuario.longValue(), tieneRol(jwt, "ADMIN")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        return documentosService.eliminar(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    private boolean tieneRol(Jwt jwt, String rol) {
        List<String> authorities = jwt.getClaimAsStringList("authorities");
        return authorities != null && authorities.contains("ROLE_" + rol);
    }
}
