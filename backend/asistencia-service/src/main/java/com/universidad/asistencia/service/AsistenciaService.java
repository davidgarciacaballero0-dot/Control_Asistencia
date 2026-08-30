package com.universidad.asistencia.service;

import com.universidad.asistencia.dto.AsistenciaResponseDto;
import com.universidad.asistencia.dto.MarcarAsistenciaQrDto;
import com.universidad.asistencia.dto.ReporteAsistenciaDto;

import java.util.List;

/**
 * Interfaz de servicio para el Caso de Uso CU07: Gestionar Asistencia.
 */
public interface AsistenciaService {

    /**
     * Registra la asistencia de un estudiante validando el codigo QR y consultando
     * al microservicio academico a traves de OpenFeign.
     *
     * @param dto Datos de la marcacion (registro de estudiante y codigo QR).
     * @return AsistenciaResponseDto con el detalle y confirmacion.
     */
    AsistenciaResponseDto registrarAsistenciaQr(MarcarAsistenciaQrDto dto);

    List<AsistenciaResponseDto> listarPorSesion(Long sesionId);

    List<AsistenciaResponseDto> listarPorEstudiante(String registroEstudiante);

    ReporteAsistenciaDto generarReporteSesion(Long sesionId);
}
