package com.practica.webAvanzada.services;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.practica.webAvanzada.models.Usuario;
import com.practica.webAvanzada.models.Alumno;
import com.practica.webAvanzada.models.Empresa;
import com.practica.webAvanzada.models.AsesorAcademico;
import com.practica.webAvanzada.models.dto.CrearCuentaAsesorRequest;
import com.practica.webAvanzada.models.dto.CrearCuentaAlumnoRequest;
import com.practica.webAvanzada.models.dto.CrearCuentaEmpresaRequest;
import com.practica.webAvanzada.models.dto.CrearUsuarioRequest;
import com.practica.webAvanzada.models.dto.ActualizarUsuarioRequest;
import com.practica.webAvanzada.repositories.AlumnoRepositorio;
import com.practica.webAvanzada.repositories.AsesorAcademicoRepositorio;
import com.practica.webAvanzada.repositories.EmpresaRepositorio;
import com.practica.webAvanzada.repositories.UsuarioRepositorio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepositorio usuarioRepositorio;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AlumnoRepositorio alumnoRepositorio;
    private final EmpresaRepositorio empresaRepositorio;
    private final AsesorAcademicoRepositorio asesorRepositorio;

    @Transactional
    public Usuario guardar(CrearUsuarioRequest solicitud) {
        String correo = solicitud.correo().trim().toLowerCase(java.util.Locale.ROOT);
        if (usuarioRepositorio.existsByCorreoIgnoreCase(correo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese correo ya tiene una cuenta.");
        }
        if (solicitud.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La contraseña no puede exceder 72 bytes UTF-8.");
        }
        Usuario usuario = new Usuario();
        usuario.setCorreo(correo);
        usuario.setPassword(passwordEncoder.encode(solicitud.password()));
        usuario.setRol(solicitud.rol());
        usuario.setActivo(true);
        try {
            return usuarioRepositorio.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese correo ya tiene una cuenta.", exception);
        }
    }

    @Transactional
    public Usuario crearCuentaAlumno(CrearCuentaAlumnoRequest solicitud) {
        String numeroControl = solicitud.numeroControl().trim();
        if (alumnoRepositorio.existsById(numeroControl)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un alumno con ese número de control.");
        }
        CrearUsuarioRequest cuenta = new CrearUsuarioRequest(solicitud.correo(), solicitud.password(),
                com.practica.webAvanzada.models.enums.RolUser.ALUMNO);
        Usuario usuario = guardar(cuenta);

        Alumno alumno = new Alumno();
        alumno.setNumeroControl(numeroControl);
        alumno.setUsuario(usuario);
        alumno.setNombreCompleto(solicitud.nombreCompleto().trim());
        alumno.setCarrera(solicitud.carrera().trim());
        alumno.setSemestre(solicitud.semestre());
        alumno.setHabilidadesTecnicas(solicitud.habilidadesTecnicas() == null
                ? "" : solicitud.habilidadesTecnicas().trim());
        try {
            alumnoRepositorio.saveAndFlush(alumno);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un alumno con ese número de control.", exception);
        }
        return usuario;
    }

    @Transactional
    public Usuario crearCuentaEmpresa(CrearCuentaEmpresaRequest solicitud) {
        Usuario usuario = guardar(new CrearUsuarioRequest(solicitud.correo(), solicitud.password(),
                com.practica.webAvanzada.models.enums.RolUser.EMPRESA));
        Empresa empresa = new Empresa();
        empresa.setUsuario(usuario);
        empresa.setRazonSocial(solicitud.razonSocial().trim());
        empresa.setSector(solicitud.sector().trim());
        empresa.setNombreCompleto(solicitud.nombreCompleto().trim());
        empresa.setTelefono(solicitud.telefono().trim());
        empresa.setDireccion(solicitud.direccion().trim());
        empresaRepositorio.saveAndFlush(empresa);
        return usuario;
    }

    @Transactional
    public Usuario crearCuentaAsesor(CrearCuentaAsesorRequest solicitud) {
        String clave = solicitud.claveAsesor().trim();
        if (asesorRepositorio.existsByClaveAsesorIgnoreCase(clave)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un asesor con esa clave.");
        }
        Usuario usuario = guardar(new CrearUsuarioRequest(solicitud.correo(), solicitud.password(),
                com.practica.webAvanzada.models.enums.RolUser.ASESOR));
        AsesorAcademico asesor = new AsesorAcademico();
        asesor.setUsuario(usuario);
        asesor.setClaveAsesor(clave);
        asesor.setNombreCompleto(solicitud.nombreCompleto().trim());
        asesor.setDepartamento(solicitud.departamento().trim());
        asesor.setMaximoAlumnos(solicitud.maximoAlumnos());
        try {
            asesorRepositorio.saveAndFlush(asesor);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un asesor con esa clave.", exception);
        }
        return usuario;
    }

    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepositorio.findAll();
    }

    public Optional<Usuario> obtenerUsuarioPorId(Long id) {
        return usuarioRepositorio.findById(id);
    }

    @Transactional
    public boolean eliminarUsuario(Long id) {
        if (!usuarioRepositorio.existsById(id)) return false;
        if (alumnoRepositorio.existsByUsuario_IdUsuario(id)
                || empresaRepositorio.existsByUsuario_IdUsuario(id)
                || asesorRepositorio.existsByUsuario_IdUsuario(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Elimina o transfiere el perfil vinculado antes de eliminar esta cuenta.");
        }
        usuarioRepositorio.deleteById(id);
        return true;
    }

    @Transactional
    public Usuario actualizarUsuario(Long id, ActualizarUsuarioRequest solicitud) {
        Optional<Usuario> usuarioExistenteOptional = usuarioRepositorio.findById(id);
        if (usuarioExistenteOptional.isEmpty()) return null;
        Usuario usuarioExistente = usuarioExistenteOptional.get();
        String correo = solicitud.correo() == null ? "" : solicitud.correo().trim().toLowerCase(java.util.Locale.ROOT);
        if (correo.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El correo es obligatorio.");
        }
        if (usuarioRepositorio.existsByCorreoIgnoreCaseAndIdUsuarioNot(correo, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese correo ya está asociado a otra cuenta.");
        }
        boolean tienePerfil = alumnoRepositorio.existsByUsuario_IdUsuario(id)
                || empresaRepositorio.existsByUsuario_IdUsuario(id)
                || asesorRepositorio.existsByUsuario_IdUsuario(id);
        if (tienePerfil && solicitud.rol() != usuarioExistente.getRol()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede cambiar el rol de una cuenta mientras tenga un perfil asociado.");
        }
        if (solicitud.password() != null && !solicitud.password().isBlank()) {
            if (solicitud.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La contraseña no puede exceder 72 bytes UTF-8.");
            }
            usuarioExistente.setPassword(passwordEncoder.encode(solicitud.password()));
        }
        usuarioExistente.setCorreo(correo);
        usuarioExistente.setRol(solicitud.rol());
        usuarioExistente.setActivo(solicitud.activo());
        try {
            return usuarioRepositorio.saveAndFlush(usuarioExistente);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese correo ya está asociado a otra cuenta.", exception);
        }
    }

    public Usuario login(String correo, String password) {
        Optional<Usuario> usuarioOptional = usuarioRepositorio.findByCorreoIgnoreCase(correo.trim());

        if (usuarioOptional.isPresent()) {
            Usuario usuario = usuarioOptional.get();

            if (usuario.isActivo() && passwordEncoder.matches(
                    password,
                    usuario.getPassword())) {

                return usuario;
            }
        }

        return null;
    }
}
