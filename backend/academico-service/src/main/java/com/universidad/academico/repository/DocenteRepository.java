package com.universidad.academico.repository;

import com.universidad.academico.domain.Docente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para la entidad Docente.
 */
@Repository
public interface DocenteRepository extends JpaRepository<Docente, String> {

    boolean existsByCorreo(String correo);
}
