package com.universidad.auth.service;

import com.universidad.auth.domain.Usuario;
import io.jsonwebtoken.Claims;

import java.util.Map;
import java.util.function.Function;

/**
 * Interfaz de servicio para operaciones con Tokens Web JSON (JWT).
 */
public interface JwtService {

    /**
     * Genera un token JWT para un usuario determinado con sus claims especificos.
     *
     * @param usuario Entidad del usuario autenticado.
     * @return Cadena con el token JWT generado.
     */
    String generarToken(Usuario usuario);

    /**
     * Extrae el nombre de usuario (subject) contenido en el token.
     *
     * @param token Token JWT.
     * @return Nombre de usuario.
     */
    String extraerUsername(String token);

    /**
     * Extrae un claim especifico del token utilizando una funcion de resolucion.
     *
     * @param token Token JWT.
     * @param claimsResolver Funcion para resolver el claim.
     * @param <T> Tipo de dato del claim.
     * @return Valor del claim resuelto.
     */
    <T> T extraerClaim(String token, Function<Claims, T> claimsResolver);

    /**
     * Valida si un token es autentico, no ha expirado y pertenece al usuario.
     *
     * @param token Token JWT.
     * @param username Nombre de usuario con el que se compara.
     * @return true si es valido, false si expiró o no corresponde.
     */
    boolean esTokenValido(String token, String username);

    /**
     * Extrae todos los claims contenidos en el cuerpo del token.
     *
     * @param token Token JWT.
     * @return Objeto Claims con la carga util.
     */
    Claims extraerTodosLosClaims(String token);
}
