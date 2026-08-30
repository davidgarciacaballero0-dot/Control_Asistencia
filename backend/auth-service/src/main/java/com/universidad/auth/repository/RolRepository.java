package com.universidad.auth.repository;

import com.universidad.auth.domain.Rol;
import com.universidad.auth.domain.RolNombre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad Rol.
 */
@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    /**
     * Busca un rol por su nombre enumerado.
     *
     * @param nombre Nombre del rol a buscar.
     * @return Optional con la entidad Rol si existe.
     */
    Optional<Rol> findByNombre(RolNombre nombre);
}
