package com.universidad.asistencia.service;

import com.universidad.asistencia.dto.IniciarSesionDto;
import com.universidad.asistencia.dto.SesionClaseDto;

import java.util.List;

/**
 * Interfaz de servicio para la gestion de Sesiones de Clase y generacion de codigos QR.
 */
public interface SesionClaseService {

    SesionClaseDto iniciarSesion(IniciarSesionDto dto);

    SesionClaseDto finalizarSesion(Long sesionId);

    SesionClaseDto regenerarCodigoQr(Long sesionId, Integer minutosValidez);

    SesionClaseDto obtenerPorId(Long sesionId);

    SesionClaseDto obtenerPorCodigoQr(String codigoQr);

    List<SesionClaseDto> listarPorGrupo(Long grupoId);

    List<SesionClaseDto> listarPorDocente(String codigoDocente);

    List<SesionClaseDto> listarActivas();
}
