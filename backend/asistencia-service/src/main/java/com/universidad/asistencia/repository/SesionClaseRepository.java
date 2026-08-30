package com.universidad.asistencia.repository;

import com.universidad.asistencia.domain.EstadoSesion;
import com.universidad.asistencia.domain.SesionClase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad SesionClase.
 */
@Repository
public interface SesionClaseRepository extends JpaRepository<SesionClase, Long> {

    Optional<SesionClase> findByCodigoQrGenerado(String codigoQrGenerado);

    List<SesionClase> findByIdGrupoReferencia(Long idGrupoReferencia);

    List<SesionClase> findByCodigoDocenteReferencia(String codigoDocenteReferencia);

    List<SesionClase> findByFecha(LocalDate fecha);

    List<SesionClase> findByIdGrupoReferenciaAndEstado(Long idGrupoReferencia, EstadoSesion estado);
}
