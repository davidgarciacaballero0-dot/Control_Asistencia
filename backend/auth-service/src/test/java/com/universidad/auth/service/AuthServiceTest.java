package com.universidad.auth.service;

import com.universidad.auth.domain.Rol;
import com.universidad.auth.domain.RolNombre;
import com.universidad.auth.domain.Usuario;
import com.universidad.auth.dto.LoginRequestDto;
import com.universidad.auth.dto.LoginResponseDto;
import com.universidad.auth.dto.TokenValidationResponseDto;
import com.universidad.auth.repository.RolRepository;
import com.universidad.auth.repository.UsuarioRepository;
import com.universidad.auth.service.impl.AuthServiceImpl;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para la logica de negocio de AuthService y validacion de tokens JWT.
 */
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private Usuario usuarioPrueba;
    private Rol rolAdmin;

    @BeforeEach
    void setUp() {
        rolAdmin = Rol.builder().id(1L).nombre(RolNombre.ROLE_ADMIN).build();

        usuarioPrueba = Usuario.builder()
                .id(1L)
                .username("admin")
                .password("$2a$10$encodedPassword")
                .nombreCompleto("Administrador")
                .email("admin@universidad.edu")
                .activo(true)
                .roles(Set.of(rolAdmin))
                .build();
    }

    @Test
    @DisplayName("Debe autenticar correctamente con credenciales validas y retornar token JWT")
    void login_ConCredencialesValidas_RetornaLoginResponse() {
        // Arrange
        LoginRequestDto request = LoginRequestDto.builder()
                .username("admin")
                .password("admin123")
                .build();

        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuarioPrueba));
        when(passwordEncoder.matches("admin123", usuarioPrueba.getPassword())).thenReturn(true);
        when(jwtService.generarToken(usuarioPrueba)).thenReturn("mocked.jwt.token");

        // Act
        LoginResponseDto response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("mocked.jwt.token", response.getToken());
        assertEquals("admin", response.getUsername());
        assertTrue(response.getRoles().contains("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("Debe lanzar excepcion si el usuario no existe")
    void login_UsuarioNoExiste_LanzaExcepcion() {
        LoginRequestDto request = LoginRequestDto.builder()
                .username("inexistente")
                .password("123")
                .build();

        when(usuarioRepository.findByUsername("inexistente")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Debe validar correctamente un token JWT activo")
    void validarToken_TokenValido_RetornaTokenValidationExitoso() {
        // Arrange
        Claims claimsMock = mock(Claims.class);
        when(claimsMock.getSubject()).thenReturn("admin");
        when(claimsMock.get("roles", List.class)).thenReturn(List.of("ROLE_ADMIN"));
        when(claimsMock.get("identificadorReferencia")).thenReturn(null);
        when(jwtService.extraerTodosLosClaims(anyString())).thenReturn(claimsMock);

        // Act
        TokenValidationResponseDto resultado = authService.validarToken("Bearer tokenValido123");

        // Assert
        assertTrue(resultado.isValid());
        assertEquals("admin", resultado.getUsername());
    }
}
