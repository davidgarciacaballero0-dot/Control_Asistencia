package com.universidad.academico.service.impl;

import com.universidad.academico.domain.Grupo;
import com.universidad.academico.domain.Horario;
import com.universidad.academico.dto.HorarioDto;
import com.universidad.academico.repository.GrupoRepository;
import com.universidad.academico.repository.HorarioRepository;
import com.universidad.academico.service.HorarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementacion del servicio de Horarios.
 */
@Service
@RequiredArgsConstructor
public class HorarioServiceImpl implements HorarioService {

    private final HorarioRepository horarioRepository;
    private final GrupoRepository grupoRepository;

    @Override
    @Transactional
    public HorarioDto agregarHorarioAGrupo(Long grupoId, HorarioDto dto) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado con ID: " + grupoId));

        if (dto.getHoraInicio().isAfter(dto.getHoraFin())) {
            throw new IllegalArgumentException("La hora de inicio no puede ser posterior a la hora de fin");
        }

        Horario horario = Horario.builder()
                .dia(dto.getDia())
                .horaInicio(dto.getHoraInicio())
                .horaFin(dto.getHoraFin())
                .grupo(grupo)
                .build();

        Horario guardado = horarioRepository.save(horario);
        return mapearADto(guardado);
    }

    @Override
    @Transactional
    public void eliminar(Long horarioId) {
        if (!horarioRepository.existsById(horarioId)) {
            throw new IllegalArgumentException("Horario no encontrado con ID: " + horarioId);
        }
        horarioRepository.deleteById(horarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HorarioDto> listarPorGrupo(Long grupoId) {
        return horarioRepository.findByGrupoId(grupoId).stream()
                .map(this::mapearADto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public HorarioDto obtenerPorId(Long horarioId) {
        return horarioRepository.findById(horarioId)
                .map(this::mapearADto)
                .orElseThrow(() -> new IllegalArgumentException("Horario no encontrado con ID: " + horarioId));
    }

    private HorarioDto mapearADto(Horario h) {
        return HorarioDto.builder()
                .id(h.getId())
                .dia(h.getDia())
                .horaInicio(h.getHoraInicio())
                .horaFin(h.getHoraFin())
                .grupoId(h.getGrupo() != null ? h.getGrupo().getId() : null)
                .build();
    }
}
