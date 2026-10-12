package com.utp.parkutp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

/** Emite y valida JWT firmados con HMAC. La clave viene del entorno. */
@Component
public class JwtService {
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey clave;
    private final long expiracionMinutos;

    public JwtService(@Value("${parkutp.jwt.secret}") String secreto,
            @Value("${parkutp.jwt.expiracion-minutos}") long expiracionMinutos) {
        byte[] bytes;
        if (secreto == null || secreto.isBlank()) {
            bytes = new byte[48];
            new SecureRandom().nextBytes(bytes);
            log.warn("JWT_SECRET no está definida: se generó una clave temporal. "
                    + "Los tokens dejan de valer al reiniciar. Defínela para la demo.");
        } else {
            bytes = secreto.getBytes(StandardCharsets.UTF_8);
            if (bytes.length < 32) {
                throw new IllegalStateException("JWT_SECRET debe tener al menos 32 caracteres");
            }
        }
        this.clave = Keys.hmacShaKeyFor(bytes);
        this.expiracionMinutos = expiracionMinutos;
    }

    public String generar(String username, Rol rol) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(username)
                .claim("rol", rol.name())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(expiracionMinutos, ChronoUnit.MINUTES)))
                .signWith(clave)
                .compact();
    }

    /** Vacío si la firma no coincide, el token está alterado o ya venció. */
    public Optional<Claims> validar(String token) {
        try {
            return Optional.of(Jwts.parser().verifyWith(clave).build()
                    .parseSignedClaims(token).getPayload());
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public long getExpiracionSegundos() {
        return expiracionMinutos * 60;
    }
}