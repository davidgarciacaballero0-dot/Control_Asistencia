package com.universidad.asistencia.service.impl;

import com.universidad.asistencia.domain.EstadoSesion;
import com.universidad.asistencia.domain.SesionClase;
import com.universidad.asistencia.dto.IniciarSesionDto;
import com.universidad.asistencia.dto.SesionClaseDto;
import com.universidad.asistencia.repository.SesionClaseRepository;
import com.universidad.asistencia.service.SesionClaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementacion de SesionClaseService aplicando Clean Architecture y principios SOLID.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SesionClaseServiceImpl implements SesionClaseService {

    private final SesionClaseRepository sesionRepository;

    @Override
    @Transactional
    public SesionClaseDto iniciarSesion(IniciarSesionDto dto) {
        int minutos = (dto.getMinutosValidezQr() != null && dto.getMinutosValidezQr() > 0)
                ? dto.getMinutosValidezQr()
                : 15;

        // Generar un token QR criptograficamente unico
        String codigoQr = "QR-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        LocalDateTime expiracion = LocalDateTime.now().plusMinutes(minutos);

        SesionClase sesion = SesionClase.builder()
                .fecha(LocalDate.now())
                .horaInicio(LocalTime.now())
                .estado(EstadoSesion.ACTIVA)
                .idHorarioReferencia(dto.getIdHorarioReferencia())
                .idGrupoReferencia(dto.getIdGrupoReferencia())
                .codigoDocenteReferencia(dto.getCodigoDocenteReferencia())
                .tema(dto.getTema() != null ? dto.getTema() : "Clase Regular")
                .codigoQrGenerado(codigoQr)
                .expiracionQr(expiracion)
                .build();

        SesionClase guardada = sesionRepository.save(sesion);
        log.info("Sesion de clase iniciada exitosamente con ID: {}, QR: {}", guardada.getId(), codigoQr);
        return mapearADto(guardada);
    }

    @Override
    @Transactional
    public SesionClaseDto finalizarSesion(Long sesionId) {
        SesionClase sesion = sesionRepository.findById(sesionId)
                .orElseThrow(() -> new IllegalArgumentException("Sesion no encontrada con ID: " + sesionId));

        sesion.setEstado(EstadoSesion.FINALIZADA);
        sesion.setHoraFin(LocalTime.now());
        sesion.setExpiracionQr(LocalDateTime.now()); // Invalida el QR inmediatamente

        SesionClase actualizada = sesionRepository.save(sesion);
        log.info("Sesion de clase finalizada con ID: {}", sesionId);
        return mapearADto(actualizada);
    }

    @Override
    @Transactional
    public SesionClaseDto regenerarCodigoQr(Long sesionId, Integer minutosValidez) {
        SesionClase sesion = sesionRepository.findById(sesionId)
                .orElseThrow(() -> new IllegalArgumentException("Sesion no encontrada con ID: " + sesionId));

        if (sesion.getEstado() != EstadoSesion.ACTIVA) {
            throw new IllegalStateException("No se puede regenerar QR para una sesion que no esta ACTIVA");
        }

        int minutos = (minutosValidez != null && minutosValidez > 0) ? minutosValidez : 15;
        String nuevoQr = "QR-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        LocalDateTime nuevaExpiracion = LocalDateTime.now().plusMinutes(minutos);

        sesion.setCodigoQrGenerado(nuevoQr);
        sesion.setExpiracionQr(nuevaExpiracion);

        SesionClase actualizada = sesionRepository.save(sesion);
        log.info("Codigo QR regenerado para la sesion ID: {}, nuevo QR: {}", sesionId, nuevoQr);
        return mapearADto(actualizada);
    }

    @Override
    @Transactional(readOnly = true)
    public SesionClaseDto obtenerPorId(Long sesionId) {
        return sesionRepository.findById(sesionId)
                .map(this::mapearADto)
                .orElseThrow(() -> new IllegalArgumentException("Sesion no encontrada con ID: " + sesionId));
    }

    @Override
    @Transactional(readOnly = true)
    public SesionClaseDto obtenerPorCodigoQr(String codigoQr) {
        return sesionRepository.findByCodigoQrGenerado(codigoQr)
                .map(this::mapearADto)
                .orElseThrow(() -> new IllegalArgumentException("Sesion no encontrada para el codigo QR proporcionado"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SesionClaseDto> listarPorGrupo(Long grupoId) {
        return sesionRepository.findByIdGrupoReferencia(grupoId).stream()
                .map(this::mapearADto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SesionClaseDto> listarPorDocente(String codigoDocente) {
        return sesionRepository.findByCodigoDocenteReferencia(codigoDocente).stream()
                .map(this::mapearADto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SesionClaseDto> listarActivas() {
        return sesionRepository.findAll().stream()
                .filter(s -> s.getEstado() == EstadoSesion.ACTIVA)
                .map(this::mapearADto)
                .toList();
    }

    private SesionClaseDto mapearADto(SesionClase s) {
        return SesionClaseDto.builder()
                .id(s.getId())
                .fecha(s.getFecha())
                .horaInicio(s.getHoraInicio())
                .horaFin(s.getHoraFin())
                .estado(s.getEstado())
                .idHorarioReferencia(s.getIdHorarioReferencia())
                .idGrupoReferencia(s.getIdGrupoReferencia())
                .codigoDocenteReferencia(s.getCodigoDocenteReferencia())
                .tema(s.getTema())
                .codigoQrGenerado(s.getCodigoQrGenerado())
                .expiracionQr(s.getExpiracionQr())
                .totalAsistenciasRegistradas(s.getAsistencias() != null ? s.getAsistencias().size() : 0)
                .build();
    }
}
