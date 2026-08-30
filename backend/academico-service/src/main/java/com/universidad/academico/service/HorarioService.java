package com.universidad.academico.service;

import com.universidad.academico.dto.HorarioDto;

import java.util.List;

/**
 * Interfaz de servicio para la gestion de Horarios.
 */
public interface HorarioService {

    HorarioDto agregarHorarioAGrupo(Long grupoId, HorarioDto dto);

    void eliminar(Long horarioId);

    List<HorarioDto> listarPorGrupo(Long grupoId);

    HorarioDto obtenerPorId(Long horarioId);
}
