package com.universidad.asistencia.repository;

import com.universidad.asistencia.domain.EstadoSesion;
import com.universidad.asistencia.domain.SesionClase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Lista todas las sesiones que se encuentran en un estado determinado ordenadas por ID descendente.
     */
    List<SesionClase> findByEstadoOrderByIdDesc(EstadoSesion estado);

    /**
     * Busca sesiones en un estado especifico que coincidan con el docente o el grupo academico.
     */
    @Query("SELECT s FROM SesionClase s WHERE s.estado = :estado AND " +
           "((:codigoDocente IS NOT NULL AND LOWER(s.codigoDocenteReferencia) = LOWER(:codigoDocente)) OR " +
           "s.idGrupoReferencia = :idGrupo)")
    List<SesionClase> findSesionesPorEstadoYDocenteOGrupo(
            @Param("estado") EstadoSesion estado,
            @Param("codigoDocente") String codigoDocente,
            @Param("idGrupo") Long idGrupo
    );
}
