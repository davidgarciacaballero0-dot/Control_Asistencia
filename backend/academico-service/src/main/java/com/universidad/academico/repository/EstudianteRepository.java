package com.universidad.academico.repository;

import com.universidad.academico.domain.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio Spring Data JPA para la entidad Estudiante.
 */
@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, String> {

    List<Estudiante> findByCarreraIgnoreCase(String carrera);

    boolean existsByCorreo(String correo);
}
