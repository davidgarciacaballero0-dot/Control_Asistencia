package com.universidad.academico.service.impl;

import com.universidad.academico.domain.Materia;
import com.universidad.academico.dto.MateriaDto;
import com.universidad.academico.repository.MateriaRepository;
import com.universidad.academico.service.MateriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementacion del servicio de Materias.
 */
@Service
@RequiredArgsConstructor
public class MateriaServiceImpl implements MateriaService {

    private final MateriaRepository materiaRepository;

    @Override
    @Transactional
    public MateriaDto crear(MateriaDto dto) {
        if (materiaRepository.existsById(dto.getSigla())) {
            throw new IllegalArgumentException("Ya existe una materia con la sigla: " + dto.getSigla());
        }

        Materia materia = Materia.builder()
                .sigla(dto.getSigla().toUpperCase())
                .nombre(dto.getNombre())
                .build();

        Materia guardada = materiaRepository.save(materia);
        return mapearADto(guardada);
    }

    @Override
    @Transactional
    public MateriaDto actualizar(String sigla, MateriaDto dto) {
        Materia existente = materiaRepository.findById(sigla)
                .orElseThrow(() -> new IllegalArgumentException("Materia no encontrada con sigla: " + sigla));

        existente.setNombre(dto.getNombre());
        Materia actualizada = materiaRepository.save(existente);
        return mapearADto(actualizada);
    }

    @Override
    @Transactional
    public void eliminar(String sigla) {
        if (!materiaRepository.existsById(sigla)) {
            throw new IllegalArgumentException("Materia no encontrada con sigla: " + sigla);
        }
        materiaRepository.deleteById(sigla);
    }

    @Override
    @Transactional(readOnly = true)
    public MateriaDto obtenerPorSigla(String sigla) {
        return materiaRepository.findById(sigla)
                .map(this::mapearADto)
                .orElseThrow(() -> new IllegalArgumentException("Materia no encontrada con sigla: " + sigla));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MateriaDto> listarTodas() {
        return materiaRepository.findAll().stream()
                .map(this::mapearADto)
                .toList();
    }

    private MateriaDto mapearADto(Materia m) {
        return MateriaDto.builder()
                .sigla(m.getSigla())
                .nombre(m.getNombre())
                .build();
    }
}
