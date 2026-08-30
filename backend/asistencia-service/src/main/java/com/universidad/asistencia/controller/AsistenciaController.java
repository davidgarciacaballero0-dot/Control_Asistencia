package com.universidad.asistencia.controller;

import com.universidad.asistencia.dto.AsistenciaResponseDto;
import com.universidad.asistencia.dto.MarcarAsistenciaQrDto;
import com.universidad.asistencia.dto.ReporteAsistenciaDto;
import com.universidad.asistencia.service.AsistenciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para el Caso de Uso CU07: Gestionar Asistencia.
 * Expone endpoints para el registro movil por escaneo de QR y consultas de asistencias.
 */
@RestController
@RequestMapping("/api/v1/asistencia/registros")
@RequiredArgsConstructor
public class AsistenciaController {

    private final AsistenciaService asistenciaService;

    /**
     * Endpoint invocado por la app movil (Flutter) para registrar la asistencia del estudiante escaneando el QR.
     *
     * @param dto Registro del estudiante y codigo QR leido por la camara.
     * @return ResponseEntity con el resultado detallado de la asistencia.
     */
    @PostMapping("/marcar-qr")
    public ResponseEntity<AsistenciaResponseDto> marcarAsistenciaQr(@Valid @RequestBody MarcarAsistenciaQrDto dto) {
        AsistenciaResponseDto response = asistenciaService.registrarAsistenciaQr(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtiene la lista de asistencias registradas en una sesion especifica.
     *
     * @param sesionId ID de la sesion de clase.
     * @return Lista de asistencias.
     */
    @GetMapping("/sesion/{sesionId}")
    public ResponseEntity<List<AsistenciaResponseDto>> listarPorSesion(@PathVariable Long sesionId) {
        return ResponseEntity.ok(asistenciaService.listarPorSesion(sesionId));
    }

    /**
     * Obtiene el historial de asistencias de un estudiante particular.
     *
     * @param registro Registro universitario del estudiante.
     * @return Lista de asistencias del estudiante.
     */
    @GetMapping("/estudiante/{registro}")
    public ResponseEntity<List<AsistenciaResponseDto>> listarPorEstudiante(@PathVariable String registro) {
        return ResponseEntity.ok(asistenciaService.listarPorEstudiante(registro));
    }

    /**
     * Genera el reporte estadistico consolidado de una sesion de clase (presentes, atrasos, total).
     *
     * @param sesionId ID de la sesion de clase.
     * @return DTO con reporte estadistico.
     */
    @GetMapping("/sesion/{sesionId}/reporte")
    public ResponseEntity<ReporteAsistenciaDto> generarReporteSesion(@PathVariable Long sesionId) {
        return ResponseEntity.ok(asistenciaService.generarReporteSesion(sesionId));
    }
}
