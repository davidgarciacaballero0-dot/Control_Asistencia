package com.universidad.auth.service.impl;

import com.universidad.auth.domain.Rol;
import com.universidad.auth.domain.Usuario;
import com.universidad.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Implementacion del servicio de JWT utilizando la biblioteca JJWT 0.12.x.
 * Proporciona firma criptografica HMAC-SHA256, generacion y validacion de tokens.
 */
@Service
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Override
    public String generarToken(Usuario usuario) {
        Map<String, Object> claimsAdicionales = new HashMap<>();
        List<String> listaRoles = usuario.getRoles().stream()
                .map(Rol::getNombre)
                .map(Enum::name)
                .toList();

        claimsAdicionales.put("roles", listaRoles);
        claimsAdicionales.put("nombreCompleto", usuario.getNombreCompleto());
        claimsAdicionales.put("email", usuario.getEmail());
        if (usuario.getIdentificadorReferencia() != null) {
            claimsAdicionales.put("identificadorReferencia", usuario.getIdentificadorReferencia());
        }

        return construirToken(claimsAdicionales, usuario.getUsername(), jwtExpiration);
    }

    private String construirToken(Map<String, Object> claimsAdicionales, String username, long expiracionMs) {
        long ahora = System.currentTimeMillis();
        return Jwts.builder()
                .claims(claimsAdicionales)
                .subject(username)
                .issuedAt(new Date(ahora))
                .expiration(new Date(ahora + expiracionMs))
                .signWith(obtenerClaveDeFirma())
                .compact();
    }

    @Override
    public String extraerUsername(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    @Override
    public <T> T extraerClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extraerTodosLosClaims(token);
        return claimsResolver.apply(claims);
    }

    @Override
    public boolean esTokenValido(String token, String username) {
        final String tokenUsername = extraerUsername(token);
        return (tokenUsername.equals(username) && !estaTokenExpirado(token));
    }

    private boolean estaTokenExpirado(String token) {
        return extraerExpiracion(token).before(new Date());
    }

    private Date extraerExpiracion(String token) {
        return extraerClaim(token, Claims::getExpiration);
    }

    @Override
    public Claims extraerTodosLosClaims(String token) {
        return Jwts.parser()
                .verifyWith(obtenerClaveDeFirma())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey obtenerClaveDeFirma() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
