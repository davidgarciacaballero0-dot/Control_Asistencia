package com.universidad.auth.controller;

import com.universidad.auth.dto.LoginRequestDto;
import com.universidad.auth.dto.LoginResponseDto;
import com.universidad.auth.dto.RegisterRequestDto;
import com.universidad.auth.dto.TokenValidationResponseDto;
import com.universidad.auth.dto.UsuarioResponseDto;
import com.universidad.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para el microservicio de autenticacion (Capa de Presentacion).
 * Expone endpoints para inicio de sesion, registro y validacion de tokens JWT.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Endpoint para iniciar sesion y obtener el token JWT.
     *
     * @param request Credenciales del usuario.
     * @return ResponseEntity con el token JWT e informacion del usuario.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        LoginResponseDto response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para registrar un nuevo usuario en el sistema.
     *
     * @param request Datos del nuevo usuario a registrar.
     * @return ResponseEntity con el usuario creado y codigo 201 CREATED.
     */
    @PostMapping("/register")
    public ResponseEntity<UsuarioResponseDto> registrar(@Valid @RequestBody RegisterRequestDto request) {
        UsuarioResponseDto response = authService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint para validar un token JWT.
     * Utilizado por el API Gateway u otros microservicios para verificar autenticidad.
     *
     * @param authHeader Cabecera Authorization con el token Bearer.
     * @return ResponseEntity con el resultado de la validacion.
     */
    @GetMapping("/validate")
    public ResponseEntity<TokenValidationResponseDto> validarToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        if (authHeader == null || authHeader.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    TokenValidationResponseDto.builder()
                            .valid(false)
                            .mensaje("Cabecera Authorization no proporcionada")
                            .build()
            );
        }

        TokenValidationResponseDto response = authService.validarToken(authHeader);
        if (!response.isValid()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene el perfil de un usuario por su nombre de usuario.
     *
     * @param username Nombre de usuario.
     * @return ResponseEntity con la informacion del usuario.
     */
    @GetMapping("/users/{username}")
    public ResponseEntity<UsuarioResponseDto> obtenerUsuario(@PathVariable String username) {
        UsuarioResponseDto usuario = authService.obtenerUsuarioPorUsername(username);
        return ResponseEntity.ok(usuario);
    }

    /**
     * Lista todos los usuarios registrados en el sistema.
     *
     * @return ResponseEntity con la lista de usuarios.
     */
    @GetMapping("/users")
    public ResponseEntity<List<UsuarioResponseDto>> listarUsuarios() {
        List<UsuarioResponseDto> usuarios = authService.listarUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    /**
     * Endpoint para aprovisionar credenciales de estudiantes u otros actores desde microservicios internos.
     *
     * @param request Datos del usuario a aprovisionar.
     * @return ResponseEntity con el usuario aprovisionado.
     */
    @PostMapping("/provisionar")
    public ResponseEntity<UsuarioResponseDto> provisionarUsuario(@Valid @RequestBody com.universidad.auth.dto.ProvisionarUsuarioDto request) {
        UsuarioResponseDto response = authService.provisionarUsuario(request);
        return ResponseEntity.ok(response);
    }
}
