package com.practica.webAvanzada.services;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import com.practica.webAvanzada.models.Usuario;
import com.practica.webAvanzada.models.dto.AuthResponse;
import com.practica.webAvanzada.models.dto.UsuarioResumen;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;

    @Value("${app.security.jwt.expiration-minutes:60}")
    private long expirationMinutes;

    public AuthResponse emitirToken(Usuario usuario) {
        Instant ahora = Instant.now();
        Instant expiracion = ahora.plusSeconds(expirationMinutes * 60);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("residencias-api")
                .issuedAt(ahora)
                .expiresAt(expiracion)
                .subject(usuario.getCorreo())
                .claim("idUsuario", usuario.getIdUsuario())
                .claim("authorities", List.of("ROLE_" + usuario.getRol().name()))
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();

        return new AuthResponse(token, "Bearer", expirationMinutes * 60,
                new UsuarioResumen(usuario.getIdUsuario(), usuario.getCorreo(), usuario.getRol()));
    }
}
