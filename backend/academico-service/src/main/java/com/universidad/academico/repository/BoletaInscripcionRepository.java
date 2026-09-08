package com.universidad.academico.repository;

import com.universidad.academico.domain.BoletaInscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad BoletaInscripcion.
 */
@Repository
public interface BoletaInscripcionRepository extends JpaRepository<BoletaInscripcion, Long> {

    List<BoletaInscripcion> findByEstudianteRegistro(String registroEstudiante);

    Optional<BoletaInscripcion> findByEstudianteRegistroAndGestion(String registroEstudiante, String gestion);

    /**
     * Consulta para verificar si un estudiante esta formalmente inscrito en un grupo determinado.
     * Esencial para la validacion cruzada de asistencia.
     *
     * @param registroEstudiante Registro del estudiante.
     * @param grupoId ID del grupo.
     * @return true si existe al menos una boleta que vincula al estudiante con el grupo.
     */
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END " +
           "FROM BoletaInscripcion b JOIN b.grupos g " +
           "WHERE b.estudiante.registro = :registroEstudiante AND g.id = :grupoId")
    boolean estaEstudianteInscritoEnGrupo(
            @Param("registroEstudiante") String registroEstudiante,
            @Param("grupoId") Long grupoId
    );

    /**
     * Obtiene la lista de estudiantes unicos inscritos en un grupo determinado.
     *
     * @param grupoId ID del grupo.
     * @return Lista de entidades Estudiante inscritas en el grupo.
     */
    @Query("SELECT DISTINCT b.estudiante FROM BoletaInscripcion b JOIN b.grupos g WHERE g.id = :grupoId")
    List<com.universidad.academico.domain.Estudiante> findEstudiantesByGrupoId(@Param("grupoId") Long grupoId);

    /**
     * Obtiene todas las boletas de inscripcion asociadas a un grupo especifico.
     * Utilizado para la sincronizacion de listas y bajas logicas de estudiantes.
     *
     * @param grupoId ID del grupo.
     * @return Lista de boletas de inscripcion vinculadas al grupo.
     */
    @Query("SELECT DISTINCT b FROM BoletaInscripcion b JOIN b.grupos g WHERE g.id = :grupoId")
    List<BoletaInscripcion> findBoletasByGrupoId(@Param("grupoId") Long grupoId);
}
