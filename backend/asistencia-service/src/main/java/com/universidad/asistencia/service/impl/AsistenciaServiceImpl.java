package com.universidad.asistencia.service.impl;

import com.universidad.asistencia.client.AcademicoFeignClient;
import com.universidad.asistencia.client.dto.VerificacionInscripcionResponse;
import com.universidad.asistencia.domain.Asistencia;
import com.universidad.asistencia.domain.EstadoAsistencia;
import com.universidad.asistencia.domain.EstadoSesion;
import com.universidad.asistencia.domain.MetodoValidacion;
import com.universidad.asistencia.domain.SesionClase;
import com.universidad.asistencia.dto.AsistenciaResponseDto;
import com.universidad.asistencia.dto.MarcarAsistenciaQrDto;
import com.universidad.asistencia.dto.ReporteAsistenciaDto;
import com.universidad.asistencia.repository.AsistenciaRepository;
import com.universidad.asistencia.repository.SesionClaseRepository;
import com.universidad.asistencia.service.AsistenciaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Implementacion de la logica de negocio de Asistencia (CU07).
 * Aplica principios de Alta Cohesion y Bajo Acoplamiento mediante comunicacion OpenFeign.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AsistenciaServiceImpl implements AsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final SesionClaseRepository sesionRepository;
    private final AcademicoFeignClient academicoClient;

    @Override
    @Transactional
    public AsistenciaResponseDto registrarAsistenciaQr(MarcarAsistenciaQrDto dto) {
        log.info("Procesando registro de asistencia QR para estudiante: {}, con codigo: {}",
                dto.getRegistroEstudiante(), dto.getCodigoQr());

        // 1. Validar existencia y estado de la sesion por su codigo QR
        SesionClase sesion = sesionRepository.findByCodigoQrGenerado(dto.getCodigoQr())
                .orElseThrow(() -> new IllegalArgumentException("El codigo QR escaneado no corresponde a ninguna sesion valida"));

        if (sesion.getEstado() != EstadoSesion.ACTIVA) {
            throw new IllegalStateException("La sesion de clase se encuentra " + sesion.getEstado() + ". No es posible registrar asistencia");
        }

        // 2. Validar vigencia temporal del QR
        if (sesion.getExpiracionQr() != null && LocalDateTime.now().isAfter(sesion.getExpiracionQr())) {
            throw new IllegalStateException("El codigo QR ha expirado. Solicite al docente que regenere el codigo");
        }

        // 3. Validar duplicidad (evitar que el mismo estudiante marque dos veces en la misma sesion)
        if (asistenciaRepository.existsBySesionClaseIdAndRegistroEstudiante(sesion.getId(), dto.getRegistroEstudiante())) {
            throw new IllegalStateException("El estudiante con registro " + dto.getRegistroEstudiante() + " ya tiene registrada su asistencia en esta sesion");
        }

        // 4. Invocacion a microservicio academico mediante OpenFeign para verificar inscripcion
        VerificacionInscripcionResponse verificacion;
        try {
            verificacion = academicoClient.verificarInscripcion(dto.getRegistroEstudiante(), sesion.getIdGrupoReferencia());
        } catch (Exception e) {
            log.error("Error al comunicarse con el microservicio academico a traves de OpenFeign: {}", e.getMessage());
            throw new IllegalStateException("No se pudo verificar la inscripcion academica. Servicio academico no disponible");
        }

        if (verificacion == null || !verificacion.isInscrito()) {
            String motivo = verificacion != null ? verificacion.getMensaje() : "Inscripcion no valida";
            throw new IllegalArgumentException("No se puede registrar asistencia: " + motivo);
        }

        // 5. Determinar estado de asistencia (PRESENTE o ATRASO segun tolerancia de 15 minutos)
        LocalTime ahora = LocalTime.now();
        long minutosTranscurridos = Duration.between(sesion.getHoraInicio(), ahora).toMinutes();
        EstadoAsistencia estadoAsistencia = (minutosTranscurridos <= 15) ? EstadoAsistencia.PRESENTE : EstadoAsistencia.ATRASO;

        // 6. Persistir el registro de asistencia en la base de datos db_asistencia
        Asistencia asistencia = Asistencia.builder()
                .fechaRegistro(LocalDate.now())
                .horaRegistro(ahora)
                .metodoValidacion(MetodoValidacion.QR)
                .registroEstudiante(dto.getRegistroEstudiante())
                .sesionClase(sesion)
                .estadoAsistencia(estadoAsistencia)
                .observacion(dto.getObservacion() != null ? dto.getObservacion() : "Registro exitoso via escaneo QR")
                .build();

        Asistencia guardada = asistenciaRepository.save(asistencia);
        log.info("Asistencia registrada exitosamente con ID: {}, Estado: {}", guardada.getId(), estadoAsistencia);

        return AsistenciaResponseDto.builder()
                .id(guardada.getId())
                .fechaRegistro(guardada.getFechaRegistro())
                .horaRegistro(guardada.getHoraRegistro())
                .metodoValidacion(guardada.getMetodoValidacion())
                .registroEstudiante(guardada.getRegistroEstudiante())
                .nombreEstudiante(verificacion.getNombreEstudiante())
                .sesionId(sesion.getId())
                .grupoId(sesion.getIdGrupoReferencia())
                .grupoNombre(verificacion.getGrupoNombre())
                .materiaSigla(verificacion.getMateriaSigla())
                .materiaNombre(verificacion.getMateriaNombre())
                .estadoAsistencia(guardada.getEstadoAsistencia())
                .mensaje("Asistencia registrada exitosamente como " + guardada.getEstadoAsistencia())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsistenciaResponseDto> listarPorSesion(Long sesionId) {
        return asistenciaRepository.findBySesionClaseId(sesionId).stream()
                .map(this::mapearADto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsistenciaResponseDto> listarPorEstudiante(String registroEstudiante) {
        return asistenciaRepository.findByRegistroEstudiante(registroEstudiante).stream()
                .map(this::mapearADto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteAsistenciaDto generarReporteSesion(Long sesionId) {
        SesionClase sesion = sesionRepository.findById(sesionId)
                .orElseThrow(() -> new IllegalArgumentException("Sesion no encontrada con ID: " + sesionId));

        List<Asistencia> asistencias = asistenciaRepository.findBySesionClaseId(sesionId);

        int totalPresentes = (int) asistencias.stream().filter(a -> a.getEstadoAsistencia() == EstadoAsistencia.PRESENTE).count();
        int totalAtrasos = (int) asistencias.stream().filter(a -> a.getEstadoAsistencia() == EstadoAsistencia.ATRASO).count();

        List<AsistenciaResponseDto> detalles = asistencias.stream()
                .map(this::mapearADto)
                .toList();

        return ReporteAsistenciaDto.builder()
                .sesionId(sesionId)
                .grupoId(sesion.getIdGrupoReferencia())
                .totalPresentes(totalPresentes)
                .totalAtrasos(totalAtrasos)
                .totalRegistrados(asistencias.size())
                .detalleAsistencias(detalles)
                .build();
    }

    private AsistenciaResponseDto mapearADto(Asistencia a) {
        return AsistenciaResponseDto.builder()
                .id(a.getId())
                .fechaRegistro(a.getFechaRegistro())
                .horaRegistro(a.getHoraRegistro())
                .metodoValidacion(a.getMetodoValidacion())
                .registroEstudiante(a.getRegistroEstudiante())
                .sesionId(a.getSesionClase() != null ? a.getSesionClase().getId() : null)
                .grupoId(a.getSesionClase() != null ? a.getSesionClase().getIdGrupoReferencia() : null)
                .estadoAsistencia(a.getEstadoAsistencia())
                .mensaje("Registro de asistencia")
                .build();
    }
}
