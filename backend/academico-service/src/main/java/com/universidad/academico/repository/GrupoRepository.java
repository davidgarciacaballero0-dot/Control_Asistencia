package com.universidad.academico.repository;

import com.universidad.academico.domain.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio Spring Data JPA para la entidad Grupo.
 */
@Repository
public interface GrupoRepository extends JpaRepository<Grupo, Long> {

    List<Grupo> findByMateriaSigla(String siglaMateria);

    List<Grupo> findByDocenteCodigo(String codigoDocente);
}
