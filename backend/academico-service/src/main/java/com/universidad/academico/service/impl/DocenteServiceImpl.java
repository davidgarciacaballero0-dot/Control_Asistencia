package com.universidad.academico.service.impl;

import com.universidad.academico.domain.Docente;
import com.universidad.academico.dto.DocenteDto;
import com.universidad.academico.repository.DocenteRepository;
import com.universidad.academico.service.DocenteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementacion del servicio de Docentes.
 */
@Service
@RequiredArgsConstructor
public class DocenteServiceImpl implements DocenteService {

    private final DocenteRepository docenteRepository;

    @Override
    @Transactional
    public DocenteDto crear(DocenteDto dto) {
        if (docenteRepository.existsById(dto.getCodigo())) {
            throw new IllegalArgumentException("Ya existe un docente con el codigo: " + dto.getCodigo());
        }
        if (docenteRepository.existsByCorreo(dto.getCorreo())) {
            throw new IllegalArgumentException("Ya existe un docente con el correo: " + dto.getCorreo());
        }

        Docente docente = Docente.builder()
                .codigo(dto.getCodigo())
                .apellidos(dto.getApellidos())
                .nombre(dto.getNombre())
                .telefono(dto.getTelefono())
                .ci(dto.getCi())
                .correo(dto.getCorreo())
                .build();

        Docente guardado = docenteRepository.save(docente);
        return mapearADto(guardado);
    }

    @Override
    @Transactional
    public DocenteDto actualizar(String codigo, DocenteDto dto) {
        Docente existente = docenteRepository.findById(codigo)
                .orElseThrow(() -> new IllegalArgumentException("Docente no encontrado con codigo: " + codigo));

        existente.setApellidos(dto.getApellidos());
        existente.setNombre(dto.getNombre());
        existente.setTelefono(dto.getTelefono());
        existente.setCi(dto.getCi());
        existente.setCorreo(dto.getCorreo());

        Docente actualizado = docenteRepository.save(existente);
        return mapearADto(actualizado);
    }

    @Override
    @Transactional
    public void eliminar(String codigo) {
        if (!docenteRepository.existsById(codigo)) {
            throw new IllegalArgumentException("Docente no encontrado con codigo: " + codigo);
        }
        docenteRepository.deleteById(codigo);
    }

    @Override
    @Transactional(readOnly = true)
    public DocenteDto obtenerPorCodigo(String codigo) {
        return docenteRepository.findById(codigo)
                .map(this::mapearADto)
                .orElseThrow(() -> new IllegalArgumentException("Docente no encontrado con codigo: " + codigo));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocenteDto> listarTodos() {
        return docenteRepository.findAll().stream()
                .map(this::mapearADto)
                .toList();
    }

    private DocenteDto mapearADto(Docente d) {
        return DocenteDto.builder()
                .codigo(d.getCodigo())
                .apellidos(d.getApellidos())
                .nombre(d.getNombre())
                .telefono(d.getTelefono())
                .ci(d.getCi())
                .correo(d.getCorreo())
                .build();
    }
}
