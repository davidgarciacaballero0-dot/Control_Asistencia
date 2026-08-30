package com.universidad.asistencia.repository;

import com.universidad.asistencia.domain.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad Asistencia.
 */
@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    List<Asistencia> findBySesionClaseId(Long sesionId);

    List<Asistencia> findByRegistroEstudiante(String registroEstudiante);

    Optional<Asistencia> findBySesionClaseIdAndRegistroEstudiante(Long sesionId, String registroEstudiante);

    boolean existsBySesionClaseIdAndRegistroEstudiante(Long sesionId, String registroEstudiante);
}
