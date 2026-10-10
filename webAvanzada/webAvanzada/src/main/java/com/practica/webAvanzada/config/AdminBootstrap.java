package com.practica.webAvanzada.config;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.practica.webAvanzada.models.Usuario;
import com.practica.webAvanzada.models.enums.RolUser;
import com.practica.webAvanzada.repositories.UsuarioRepositorio;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);
    private static final Pattern CORREO_VALIDO = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final UsuarioRepositorio usuarioRepositorio;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin.email:}")
    private String correoConfigurado;

    @Value("${app.bootstrap.admin.password:}")
    private String passwordConfigurada;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarioRepositorio.existsByRol(RolUser.ADMIN)) return;

        String correo = correoConfigurado == null ? "" : correoConfigurado.trim().toLowerCase(Locale.ROOT);
        String password = passwordConfigurada == null ? "" : passwordConfigurada;

        if (correo.isBlank() && password.isBlank()) return;
        if (correo.isBlank() || password.isBlank()) {
            throw new IllegalStateException("Configura ADMIN_BOOTSTRAP_EMAIL y ADMIN_BOOTSTRAP_PASSWORD juntos.");
        }
        if (correo.length() > 254 || !CORREO_VALIDO.matcher(correo).matches()) {
            throw new IllegalStateException("ADMIN_BOOTSTRAP_EMAIL debe ser un correo válido de hasta 254 caracteres.");
        }
        int longitudPassword = password.getBytes(StandardCharsets.UTF_8).length;
        if (password.length() < 8 || longitudPassword > 72) {
            throw new IllegalStateException("ADMIN_BOOTSTRAP_PASSWORD debe tener al menos 8 caracteres y máximo 72 bytes UTF-8.");
        }

        if (usuarioRepositorio.existsByCorreoIgnoreCase(correo)) {
            throw new IllegalStateException("El correo de aprovisionamiento ya pertenece a una cuenta que no es ADMIN.");
        }

        Usuario administrador = new Usuario();
        administrador.setCorreo(correo);
        administrador.setPassword(passwordEncoder.encode(password));
        administrador.setRol(RolUser.ADMIN);
        administrador.setActivo(true);
        usuarioRepositorio.saveAndFlush(administrador);
        log.info("Se creó la cuenta ADMIN inicial. Las credenciales no se registran en logs.");
    }
}
