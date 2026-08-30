package com.universidad.academico.service.impl;

import com.universidad.academico.domain.Estudiante;
import com.universidad.academico.dto.EstudianteDto;
import com.universidad.academico.repository.EstudianteRepository;
import com.universidad.academico.service.EstudianteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementacion del servicio de Estudiantes aplicando principios SOLID y Clean Architecture.
 */
@Service
@RequiredArgsConstructor
public class EstudianteServiceImpl implements EstudianteService {

    private final EstudianteRepository estudianteRepository;

    @Override
    @Transactional
    public EstudianteDto crear(EstudianteDto dto) {
        if (estudianteRepository.existsById(dto.getRegistro())) {
            throw new IllegalArgumentException("Ya existe un estudiante con el registro: " + dto.getRegistro());
        }
        if (estudianteRepository.existsByCorreo(dto.getCorreo())) {
            throw new IllegalArgumentException("Ya existe un estudiante con el correo: " + dto.getCorreo());
        }

        Estudiante estudiante = Estudiante.builder()
                .registro(dto.getRegistro())
                .apellidos(dto.getApellidos())
                .nombre(dto.getNombre())
                .telefono(dto.getTelefono())
                .ci(dto.getCi())
                .correo(dto.getCorreo())
                .carrera(dto.getCarrera())
                .plan(dto.getPlan())
                .build();

        Estudiante guardado = estudianteRepository.save(estudiante);
        return mapearADto(guardado);
    }

    @Override
    @Transactional
    public EstudianteDto actualizar(String registro, EstudianteDto dto) {
        Estudiante existente = estudianteRepository.findById(registro)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado con registro: " + registro));

        existente.setApellidos(dto.getApellidos());
        existente.setNombre(dto.getNombre());
        existente.setTelefono(dto.getTelefono());
        existente.setCi(dto.getCi());
        existente.setCorreo(dto.getCorreo());
        existente.setCarrera(dto.getCarrera());
        existente.setPlan(dto.getPlan());

        Estudiante actualizado = estudianteRepository.save(existente);
        return mapearADto(actualizado);
    }

    @Override
    @Transactional
    public void eliminar(String registro) {
        if (!estudianteRepository.existsById(registro)) {
            throw new IllegalArgumentException("Estudiante no encontrado con registro: " + registro);
        }
        estudianteRepository.deleteById(registro);
    }

    @Override
    @Transactional(readOnly = true)
    public EstudianteDto obtenerPorRegistro(String registro) {
        return estudianteRepository.findById(registro)
                .map(this::mapearADto)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado con registro: " + registro));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstudianteDto> listarTodos() {
        return estudianteRepository.findAll().stream()
                .map(this::mapearADto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstudianteDto> listarPorCarrera(String carrera) {
        return estudianteRepository.findByCarreraIgnoreCase(carrera).stream()
                .map(this::mapearADto)
                .toList();
    }

    private EstudianteDto mapearADto(Estudiante e) {
        return EstudianteDto.builder()
                .registro(e.getRegistro())
                .apellidos(e.getApellidos())
                .nombre(e.getNombre())
                .telefono(e.getTelefono())
                .ci(e.getCi())
                .correo(e.getCorreo())
                .carrera(e.getCarrera())
                .plan(e.getPlan())
                .build();
    }
}
