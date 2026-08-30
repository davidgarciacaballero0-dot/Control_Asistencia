package com.universidad.auth.service.impl;

import com.universidad.auth.domain.Rol;
import com.universidad.auth.domain.RolNombre;
import com.universidad.auth.domain.Usuario;
import com.universidad.auth.dto.LoginRequestDto;
import com.universidad.auth.dto.LoginResponseDto;
import com.universidad.auth.dto.RegisterRequestDto;
import com.universidad.auth.dto.TokenValidationResponseDto;
import com.universidad.auth.dto.UsuarioResponseDto;
import com.universidad.auth.repository.RolRepository;
import com.universidad.auth.repository.UsuarioRepository;
import com.universidad.auth.service.AuthService;
import com.universidad.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementacion de la logica de negocio para autenticacion y gestion de usuarios.
 * Aplica principios SOLID e inyeccion de dependencias por constructor.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginRequestDto request) {
        // 1. Buscar usuario por username
        Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales invalidas: usuario no encontrado"));

        // 2. Verificar estado activo
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new IllegalStateException("La cuenta de usuario se encuentra inactiva");
        }

        // 3. Validar contrasena con BCrypt
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new IllegalArgumentException("Credenciales invalidas: contrasena incorrecta");
        }

        // 4. Generar token JWT
        String token = jwtService.generarToken(usuario);

        // 5. Extraer nombres de roles para el DTO
        List<String> roles = usuario.getRoles().stream()
                .map(r -> r.getNombre().name())
                .toList();

        return LoginResponseDto.builder()
                .token(token)
                .tipo("Bearer")
                .username(usuario.getUsername())
                .nombreCompleto(usuario.getNombreCompleto())
                .email(usuario.getEmail())
                .ci(usuario.getCi())
                .identificadorReferencia(usuario.getIdentificadorReferencia())
                .roles(roles)
                .build();
    }

    @Override
    @Transactional
    public UsuarioResponseDto registrar(RegisterRequestDto request) {
        // 1. Validar unicidad de username y email
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("El nombre de usuario '" + request.getUsername() + "' ya esta en uso");
        }
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El correo electronico '" + request.getEmail() + "' ya esta registrado");
        }

        // 2. Resolver roles solicitados
        Set<Rol> rolesAsignados = new HashSet<>();
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            // Rol por defecto si no se especifica: ROLE_ESTUDIANTE
            Rol rolPorDefecto = rolRepository.findByNombre(RolNombre.ROLE_ESTUDIANTE)
                    .orElseGet(() -> rolRepository.save(Rol.builder().nombre(RolNombre.ROLE_ESTUDIANTE).build()));
            rolesAsignados.add(rolPorDefecto);
        } else {
            for (String nombreRolStr : request.getRoles()) {
                try {
                    RolNombre rolEnum = RolNombre.valueOf(nombreRolStr.toUpperCase());
                    Rol rol = rolRepository.findByNombre(rolEnum)
                            .orElseGet(() -> rolRepository.save(Rol.builder().nombre(rolEnum).build()));
                    rolesAsignados.add(rol);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Rol invalido proporcionado: " + nombreRolStr);
                }
            }
        }

        // 3. Crear entidad con contrasena encriptada
        Usuario nuevoUsuario = Usuario.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .nombreCompleto(request.getNombreCompleto())
                .email(request.getEmail())
                .ci(request.getCi())
                .identificadorReferencia(request.getIdentificadorReferencia())
                .activo(true)
                .roles(rolesAsignados)
                .build();

        Usuario guardado = usuarioRepository.save(nuevoUsuario);

        return mapearAUsuarioResponseDto(guardado);
    }

    @Override
    public TokenValidationResponseDto validarToken(String token) {
        try {
            // Limpiar prefijo Bearer si viene incluido
            String tokenLimpio = token.startsWith("Bearer ") ? token.substring(7) : token;
            Claims claims = jwtService.extraerTodosLosClaims(tokenLimpio);
            String username = claims.getSubject();

            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) claims.get("roles", List.class);
            String referencia = (String) claims.get("identificadorReferencia");

            return TokenValidationResponseDto.builder()
                    .valid(true)
                    .username(username)
                    .identificadorReferencia(referencia)
                    .roles(roles)
                    .mensaje("Token valido y activo")
                    .build();
        } catch (Exception e) {
            return TokenValidationResponseDto.builder()
                    .valid(false)
                    .mensaje("Token invalido o expirado: " + e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDto obtenerUsuarioPorUsername(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));
        return mapearAUsuarioResponseDto(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDto> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(this::mapearAUsuarioResponseDto)
                .toList();
    }

    private UsuarioResponseDto mapearAUsuarioResponseDto(Usuario usuario) {
        List<String> roles = usuario.getRoles().stream()
                .map(r -> r.getNombre().name())
                .toList();

        return UsuarioResponseDto.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .nombreCompleto(usuario.getNombreCompleto())
                .email(usuario.getEmail())
                .ci(usuario.getCi())
                .identificadorReferencia(usuario.getIdentificadorReferencia())
                .activo(usuario.getActivo())
                .roles(roles)
                .build();
    }
}
