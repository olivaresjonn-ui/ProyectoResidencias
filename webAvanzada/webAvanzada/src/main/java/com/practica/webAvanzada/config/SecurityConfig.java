package com.practica.webAvanzada.config;

import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecretKey jwtSecretKey(@Value("${app.security.jwt.secret}") String base64Secret) {
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(base64Secret);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("JWT_SECRET debe ser una clave codificada en Base64.", exception);
        }
        if (decoded.length < 32) {
            throw new IllegalStateException("JWT_SECRET debe representar al menos 32 bytes aleatorios.");
        }
        return new SecretKeySpec(decoded, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(jwtSecretKey));
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        return NimbusJwtDecoder.withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("authorities");
        authoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/me").authenticated()
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                        .requestMatchers("/api/analitica", "/api/analitica/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/proyectos/mios").hasRole("EMPRESA")
                        .requestMatchers(HttpMethod.GET, "/api/proyectos/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/proyectos/**").hasAnyRole("EMPRESA", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/proyectos/**").hasAnyRole("EMPRESA", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/proyectos/**").hasAnyRole("EMPRESA", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/postulaciones/mias").hasRole("ALUMNO")
                        .requestMatchers(HttpMethod.POST, "/api/postulaciones/**").hasRole("ALUMNO")
                        .requestMatchers(HttpMethod.GET, "/api/postulaciones", "/api/postulaciones/**")
                                .hasAnyRole("ASESOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/postulaciones/**").hasAnyRole("ASESOR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/postulaciones/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/documentos/**").hasRole("ALUMNO")
                        .requestMatchers(HttpMethod.GET, "/api/documentos/**").hasAnyRole("ALUMNO", "ASESOR", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/documentos/**").hasAnyRole("ASESOR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/documentos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/alumnos/mio").hasRole("ALUMNO")
                        .requestMatchers(HttpMethod.PATCH, "/api/alumnos/mio/habilidades").hasRole("ALUMNO")
                        .requestMatchers(HttpMethod.PUT, "/api/alumnos/*/asesor").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/alumnos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/alumnos/**").hasAnyRole("ASESOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/alumnos/**").hasAnyRole("ASESOR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/alumnos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/empresas/mia").hasRole("EMPRESA")
                        .requestMatchers("/api/empresas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/asesores/mio").hasRole("ASESOR")
                        .requestMatchers("/api/asesores/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable);
        return http.build();
    }
}
