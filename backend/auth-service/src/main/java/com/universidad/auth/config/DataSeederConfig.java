package com.universidad.auth.config;

import com.universidad.auth.domain.Rol;
import com.universidad.auth.domain.RolNombre;
import com.universidad.auth.domain.Usuario;
import com.universidad.auth.repository.RolRepository;
import com.universidad.auth.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

/**
 * Componente para sembrar datos iniciales (Seeders) en la base de datos db_auth.
 * Facilita las pruebas inmediatas del sistema sin necesidad de registros manuales.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeederConfig implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // 1. Inicializar roles si no existen
        Rol rolAdmin = obtenerOCrearRol(RolNombre.ROLE_ADMIN);
        Rol rolDocente = obtenerOCrearRol(RolNombre.ROLE_DOCENTE);
        Rol rolEstudiante = obtenerOCrearRol(RolNombre.ROLE_ESTUDIANTE);

        // 2. Crear usuario Administrador
        crearUsuarioSiNoExiste(
                "admin",
                "admin123",
                "Administrador del Sistema",
                "admin@universidad.edu",
                null,
                Set.of(rolAdmin)
        );

        // 3. Crear usuario Docente
        crearUsuarioSiNoExiste(
                "DOC-101",
                "docente123",
                "Dr. Roberto Mendoza Ramos",
                "roberto.mendoza@universidad.edu",
                "DOC-101",
                Set.of(rolDocente)
        );

        // 4. Crear usuarios Estudiantes
        crearUsuarioSiNoExiste(
                "2024001",
                "estudiante123",
                "Juan Perez Gomez",
                "juan.perez@estudiante.edu",
                "2024001",
                Set.of(rolEstudiante)
        );

        crearUsuarioSiNoExiste(
                "2024002",
                "estudiante123",
                "Maria Garcia Lopez",
                "maria.garcia@estudiante.edu",
                "2024002",
                Set.of(rolEstudiante)
        );

        log.info("Datos iniciales de roles y usuarios sembrados correctamente");
    }

    private Rol obtenerOCrearRol(RolNombre nombre) {
        return rolRepository.findByNombre(nombre)
                .orElseGet(() -> rolRepository.save(Rol.builder().nombre(nombre).build()));
    }

    private void crearUsuarioSiNoExiste(
            String username,
            String passwordPlano,
            String nombreCompleto,
            String email,
            String identificadorReferencia,
            Set<Rol> roles
    ) {
        if (!usuarioRepository.existsByUsername(username)) {
            Usuario usuario = Usuario.builder()
                    .username(username)
                    .password(passwordEncoder.encode(passwordPlano))
                    .nombreCompleto(nombreCompleto)
                    .email(email)
                    .identificadorReferencia(identificadorReferencia)
                    .activo(true)
                    .roles(roles)
                    .build();
            usuarioRepository.save(usuario);
            log.info("Usuario creado: {}", username);
        }
    }
}
