package com.universidad.academico.service;

import com.universidad.academico.dto.BoletaInscripcionDto;
import com.universidad.academico.dto.VerificacionInscripcionDto;

import java.util.List;

/**
 * Interfaz de servicio para la gestion de Boletas de Inscripcion (CU06) y verificaciones cruzadas.
 */
public interface BoletaInscripcionService {

    BoletaInscripcionDto inscribirEstudiante(BoletaInscripcionDto dto);

    BoletaInscripcionDto obtenerPorNumero(Long numero);

    List<BoletaInscripcionDto> listarPorEstudiante(String registroEstudiante);

    List<BoletaInscripcionDto> listarTodas();

    void anularInscripcion(Long numero);

    /**
     * Verifica si un estudiante esta registrado e inscrito en un grupo para marcar asistencia.
     * Metodo consumido via REST / Feign Client por el MS Asistencia.
     *
     * @param registroEstudiante Registro del estudiante.
     * @param grupoId ID del grupo al que asiste.
     * @return DTO con el resultado de la verificacion y datos academicos.
     */
    VerificacionInscripcionDto verificarInscripcionEstudianteEnGrupo(String registroEstudiante, Long grupoId);
}
