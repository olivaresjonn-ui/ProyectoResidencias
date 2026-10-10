package com.practica.webAvanzada.services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.practica.webAvanzada.models.ExpedientesDocumentos;
import com.practica.webAvanzada.models.Postulaciones;
import com.practica.webAvanzada.models.dto.DocumentoResumen;
import com.practica.webAvanzada.models.enums.EstadoAprobacion;
import com.practica.webAvanzada.models.enums.TipoDocumento;
import com.practica.webAvanzada.repositories.ExpedientesDocumentosRepositorio;
import com.practica.webAvanzada.repositories.PostulacionesRepositorio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExpedientesDocumentosService {

    private static final long TAMANO_MAXIMO = 10L * 1024 * 1024;

    private final ExpedientesDocumentosRepositorio documentosRepositorio;
    private final PostulacionesRepositorio postulacionesRepositorio;

    @Value("${app.documents.storage-path:./storage/documentos}")
    private String storagePath;

    @Transactional
    public DocumentoResumen crear(Long idPostulacion, TipoDocumento tipoDocumento, MultipartFile archivo, Long idUsuario) {
        Postulaciones postulacion = postulacionesRepositorio.findById(idPostulacion)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe esa postulación."));

        if (!postulacion.getAlumno().getUsuario().getIdUsuario().equals(idUsuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo puedes subir documentos a tu propio expediente.");
        }
        validarPdf(archivo);

        Path directorio = Path.of(storagePath).toAbsolutePath().normalize();
        String nombreGuardado = UUID.randomUUID() + ".pdf";
        Path destino = directorio.resolve(nombreGuardado).normalize();
        if (!destino.startsWith(directorio)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La ruta del archivo no es válida.");
        }

        try {
            Files.createDirectories(directorio);
            try (var entrada = archivo.getInputStream()) {
                Files.copy(entrada, destino);
            }

            ExpedientesDocumentos documento = new ExpedientesDocumentos();
            documento.setFolioDocumento("DOC-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
            documento.setPostulacion(postulacion);
            documento.setTipoDocumento(tipoDocumento);
            documento.setRutaArchivoUrl(nombreGuardado);
            documento.setFechaSubida(Date.valueOf(LocalDate.now()));
            documento.setEstadoAprobacion(EstadoAprobacion.PENDIENTE);
            return resumir(documentosRepositorio.saveAndFlush(documento));
        } catch (IOException exception) {
            eliminarArchivo(destino);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo guardar el archivo.", exception);
        } catch (RuntimeException exception) {
            eliminarArchivo(destino);
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public List<DocumentoResumen> obtenerTodos(Long idUsuario, boolean administrador, boolean asesor) {
        List<ExpedientesDocumentos> documentos = administrador
                ? documentosRepositorio.findAll()
                : asesor
                        ? documentosRepositorio.findByPostulacion_Alumno_AsesorAcademico_Usuario_IdUsuario(idUsuario)
                        : documentosRepositorio.findByPostulacion_Alumno_Usuario_IdUsuario(idUsuario);
        return documentos.stream().map(this::resumir).toList();
    }

    @Transactional(readOnly = true)
    public Optional<DocumentoResumen> obtenerPorId(Long id, Long idUsuario, boolean administrador) {
        return documentosRepositorio.findById(id)
                .map(documento -> {
                    verificarPermiso(documento, idUsuario, administrador);
                    return resumir(documento);
                });
    }

    @Transactional(readOnly = true)
    public Path obtenerArchivo(Long id, Long idUsuario, boolean administrador) {
        ExpedientesDocumentos documento = documentosRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe ese documento."));
        verificarPermiso(documento, idUsuario, administrador);

        Path directorio = Path.of(storagePath).toAbsolutePath().normalize();
        Path archivo = directorio.resolve(documento.getRutaArchivoUrl()).normalize();
        if (!archivo.startsWith(directorio) || !Files.isRegularFile(archivo)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El archivo no está disponible.");
        }
        return archivo;
    }

    @Transactional
    public DocumentoResumen actualizarEstado(Long id, EstadoAprobacion estado, Long idUsuario, boolean administrador) {
        ExpedientesDocumentos documento = documentosRepositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe ese documento."));
        verificarPermiso(documento, idUsuario, administrador);
        documento.setEstadoAprobacion(estado);
        return resumir(documentosRepositorio.save(documento));
    }

    @Transactional
    public boolean eliminar(Long id) {
        ExpedientesDocumentos documento = documentosRepositorio.findById(id).orElse(null);
        if (documento == null) return false;
        Path archivo = Path.of(storagePath).toAbsolutePath().normalize().resolve(documento.getRutaArchivoUrl()).normalize();
        documentosRepositorio.delete(documento);
        eliminarArchivo(archivo);
        return true;
    }

    private void validarPdf(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona un archivo PDF.");
        }
        if (archivo.getSize() > TAMANO_MAXIMO) {
            throw new ResponseStatusException(HttpStatus.CONTENT_TOO_LARGE, "El archivo no puede superar 10 MB.");
        }
        String nombre = Optional.ofNullable(archivo.getOriginalFilename()).orElse("");
        if (!nombre.toLowerCase().endsWith(".pdf") || !"application/pdf".equalsIgnoreCase(archivo.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se aceptan archivos PDF.");
        }
        try (var entrada = archivo.getInputStream()) {
            String firma = new String(entrada.readNBytes(5), StandardCharsets.US_ASCII);
            if (!"%PDF-".equals(firma)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El contenido del archivo no es un PDF válido.");
            }
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer el archivo.", exception);
        }
    }

    private void verificarPermiso(ExpedientesDocumentos documento, Long idUsuario, boolean administrador) {
        boolean esPropietario = documento.getPostulacion().getAlumno().getUsuario().getIdUsuario().equals(idUsuario);
        var asesor = documento.getPostulacion().getAlumno().getAsesorAcademico();
        boolean asesorAsignado = asesor != null && asesor.getUsuario().getIdUsuario().equals(idUsuario);
        if (!administrador && !esPropietario && !asesorAsignado) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes acceso a este documento.");
        }
    }

    private DocumentoResumen resumir(ExpedientesDocumentos documento) {
        return new DocumentoResumen(documento.getIdDocumento(), documento.getFolioDocumento(),
                documento.getPostulacion().getIdPostulacion(), documento.getTipoDocumento(),
                documento.getFechaSubida(), documento.getEstadoAprobacion());
    }

    private void eliminarArchivo(Path archivo) {
        try {
            Files.deleteIfExists(archivo);
        } catch (IOException ignored) {
            // Si falla la limpieza, el identificador aleatorio no es público y el archivo no queda expuesto por HTTP.
        }
    }
}
