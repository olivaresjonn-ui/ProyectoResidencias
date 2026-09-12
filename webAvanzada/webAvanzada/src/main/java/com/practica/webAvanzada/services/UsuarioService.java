package com.practica.webAvanzada.services;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.practica.webAvanzada.models.Usuario;
import com.practica.webAvanzada.repositories.UsuarioRepositorio;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepositorio usuarioRepositorio;
    private final BCryptPasswordEncoder passwordEncoder;

    public Usuario guardar(Usuario usuario) {
        usuario.setPassword(
                passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepositorio.save(usuario);
    }

    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepositorio.findAll();
    }

    public Optional<Usuario> obtenerUsuarioPorId(Long id) {
        return usuarioRepositorio.findById(id);
    }

    public boolean eliminarUsuario(Long id) {
        if (!usuarioRepositorio.existsById(id)) {
            return false;
        }
        usuarioRepositorio.deleteById(id);
        return true;
    }

    public Usuario actualizarUsuario(Long id, Usuario usuarioActualizado) {
        Optional<Usuario> usuarioExistenteOptional = usuarioRepositorio.findById(id);

        if (usuarioExistenteOptional.isPresent()) {
            Usuario usuarioExistente = usuarioExistenteOptional.get();

            usuarioExistente.setCorreo(
                    usuarioActualizado.getCorreo());

            usuarioExistente.setPassword(
                    passwordEncoder.encode(
                            usuarioActualizado.getPassword()));

            usuarioExistente.setRol(
                    usuarioActualizado.getRol());

            return usuarioRepositorio.save(usuarioExistente);

        } else {
            return null;
        }
    }

    public Usuario login(String correo, String password) {
        Optional<Usuario> usuarioOptional = usuarioRepositorio.findByCorreo(correo);

        if (usuarioOptional.isPresent()) {
            Usuario usuario = usuarioOptional.get();

            if (passwordEncoder.matches(
                    password,
                    usuario.getPassword())) {

                return usuario;
            }
        }

        return null;
    }
}
