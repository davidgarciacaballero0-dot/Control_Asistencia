package com.universidad.auth.repository;

import com.universidad.auth.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad Usuario.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su nombre de usuario.
     *
     * @param username Nombre de usuario.
     * @return Optional con el usuario si existe.
     */
    Optional<Usuario> findByUsername(String username);

    /**
     * Busca un usuario por su correo electronico.
     *
     * @param email Correo electronico.
     * @return Optional con el usuario si existe.
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Verifica si ya existe un usuario con el nombre de usuario dado.
     *
     * @param username Nombre de usuario.
     * @return true si existe, false en caso contrario.
     */
    boolean existsByUsername(String username);

    /**
     * Verifica si ya existe un usuario con el correo electronico dado.
     *
     * @param email Correo electronico.
     * @return true si existe, false en caso contrario.
     */
    boolean existsByEmail(String email);
}
