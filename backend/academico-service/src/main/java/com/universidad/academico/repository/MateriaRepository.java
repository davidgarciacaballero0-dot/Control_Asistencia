package com.universidad.academico.repository;

import com.universidad.academico.domain.Materia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para la entidad Materia.
 */
@Repository
public interface MateriaRepository extends JpaRepository<Materia, String> {
}
