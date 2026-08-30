package com.universidad.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

/**
 * Componente utilitario en el API Gateway para validar y parsear tokens JWT.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    public void validarToken(String token) {
        Jwts.parser()
                .verifyWith(obtenerClaveDeFirma())
                .build()
                .parseSignedClaims(token);
    }

    public Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(obtenerClaveDeFirma())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extraerUsername(String token) {
        return extraerClaims(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> extraerRoles(String token) {
        return (List<String>) extraerClaims(token).get("roles", List.class);
    }

    public String extraerIdentificadorReferencia(String token) {
        return (String) extraerClaims(token).get("identificadorReferencia");
    }

    public boolean estaExpirado(String token) {
        return extraerClaims(token).getExpiration().before(new Date());
    }

    private SecretKey obtenerClaveDeFirma() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
