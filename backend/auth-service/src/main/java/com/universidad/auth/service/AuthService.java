package com.universidad.auth.service;

import com.universidad.auth.dto.LoginRequestDto;
import com.universidad.auth.dto.LoginResponseDto;
import com.universidad.auth.dto.RegisterRequestDto;
import com.universidad.auth.dto.TokenValidationResponseDto;
import com.universidad.auth.dto.UsuarioResponseDto;

import java.util.List;

/**
 * Interfaz de servicio de aplicacion para la logica de negocio de autenticacion y gestion de usuarios.
 */
public interface AuthService {

    /**
     * Autentica a un usuario y genera su token JWT correspondiente.
     *
     * @param request Datos de credenciales (usuario y contrasena).
     * @return DTO con los datos del usuario autenticado y el token JWT.
     */
    LoginResponseDto login(LoginRequestDto request);

    /**
     * Registra un nuevo usuario en la base de datos con contrasena cifrada y roles asignados.
     *
     * @param request Datos del nuevo usuario.
     * @return DTO con la informacion del usuario creado.
     */
    UsuarioResponseDto registrar(RegisterRequestDto request);

    /**
     * Valida un token JWT extrayendo su validez y permisos.
     *
     * @param token Token JWT recibido en las cabeceras.
     * @return DTO con el resultado de la validacion.
     */
    TokenValidationResponseDto validarToken(String token);

    /**
     * Obtiene los datos de un usuario por su nombre de usuario.
     *
     * @param username Nombre de usuario.
     * @return Informacion del usuario.
     */
    UsuarioResponseDto obtenerUsuarioPorUsername(String username);

    /**
     * Obtiene la lista completa de usuarios registrados.
     *
     * @return Lista de DTOs de usuarios.
     */
    List<UsuarioResponseDto> listarUsuarios();

    /**
     * Aprovisiona o actualiza una cuenta de usuario de forma idempotente.
     *
     * @param request Datos del usuario a aprovisionar.
     * @return DTO con la informacion del usuario.
     */
    UsuarioResponseDto provisionarUsuario(com.universidad.auth.dto.ProvisionarUsuarioDto request);
}
