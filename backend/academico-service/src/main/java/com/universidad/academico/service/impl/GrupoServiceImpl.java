package com.universidad.academico.service.impl;

import com.universidad.academico.domain.Docente;
import com.universidad.academico.domain.Grupo;
import com.universidad.academico.domain.Horario;
import com.universidad.academico.domain.Materia;
import com.universidad.academico.dto.GrupoDto;
import com.universidad.academico.dto.HorarioDto;
import com.universidad.academico.repository.DocenteRepository;
import com.universidad.academico.repository.GrupoRepository;
import com.universidad.academico.repository.MateriaRepository;
import com.universidad.academico.service.GrupoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementacion del servicio de Grupos academicos.
 */
@Service
@RequiredArgsConstructor
public class GrupoServiceImpl implements GrupoService {

    private final GrupoRepository grupoRepository;
    private final MateriaRepository materiaRepository;
    private final DocenteRepository docenteRepository;

    @Override
    @Transactional
    public GrupoDto crear(GrupoDto dto) {
        Materia materia = materiaRepository.findById(dto.getMateriaSigla().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Materia no encontrada: " + dto.getMateriaSigla()));

        Docente docente = docenteRepository.findById(dto.getDocenteCodigo())
                .orElseThrow(() -> new IllegalArgumentException("Docente no encontrado: " + dto.getDocenteCodigo()));

        Grupo grupo = Grupo.builder()
                .nombre(dto.getNombre())
                .cupo(dto.getCupo())
                .materia(materia)
                .docente(docente)
                .horarios(new ArrayList<>())
                .build();

        if (dto.getHorarios() != null && !dto.getHorarios().isEmpty()) {
            for (HorarioDto hDto : dto.getHorarios()) {
                Horario horario = Horario.builder()
                        .dia(hDto.getDia())
                        .horaInicio(hDto.getHoraInicio())
                        .horaFin(hDto.getHoraFin())
                        .grupo(grupo)
                        .build();
                grupo.getHorarios().add(horario);
            }
        }

        Grupo guardado = grupoRepository.save(grupo);
        return mapearADto(guardado);
    }

    @Override
    @Transactional
    public GrupoDto actualizar(Long id, GrupoDto dto) {
        Grupo existente = grupoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado con ID: " + id));

        Materia materia = materiaRepository.findById(dto.getMateriaSigla().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Materia no encontrada: " + dto.getMateriaSigla()));

        Docente docente = docenteRepository.findById(dto.getDocenteCodigo())
                .orElseThrow(() -> new IllegalArgumentException("Docente no encontrado: " + dto.getDocenteCodigo()));

        existente.setNombre(dto.getNombre());
        existente.setCupo(dto.getCupo());
        existente.setMateria(materia);
        existente.setDocente(docente);

        Grupo actualizado = grupoRepository.save(existente);
        return mapearADto(actualizado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!grupoRepository.existsById(id)) {
            throw new IllegalArgumentException("Grupo no encontrado con ID: " + id);
        }
        grupoRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public GrupoDto obtenerPorId(Long id) {
        return grupoRepository.findById(id)
                .map(this::mapearADto)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GrupoDto> listarTodos() {
        return grupoRepository.findAll().stream()
                .map(this::mapearADto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GrupoDto> listarPorMateria(String siglaMateria) {
        return grupoRepository.findByMateriaSigla(siglaMateria.toUpperCase()).stream()
                .map(this::mapearADto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GrupoDto> listarPorDocente(String codigoDocente) {
        return grupoRepository.findByDocenteCodigo(codigoDocente).stream()
                .map(this::mapearADto)
                .toList();
    }

    private GrupoDto mapearADto(Grupo g) {
        List<HorarioDto> horariosDto = g.getHorarios() != null
                ? g.getHorarios().stream().map(h -> HorarioDto.builder()
                .id(h.getId())
                .dia(h.getDia())
                .horaInicio(h.getHoraInicio())
                .horaFin(h.getHoraFin())
                .grupoId(g.getId())
                .build()).toList()
                : List.of();

        return GrupoDto.builder()
                .id(g.getId())
                .nombre(g.getNombre())
                .cupo(g.getCupo())
                .materiaSigla(g.getMateria().getSigla())
                .materiaNombre(g.getMateria().getNombre())
                .docenteCodigo(g.getDocente().getCodigo())
                .docenteNombreCompleto(g.getDocente().getNombre() + " " + g.getDocente().getApellidos())
                .horarios(horariosDto)
                .build();
    }
}
