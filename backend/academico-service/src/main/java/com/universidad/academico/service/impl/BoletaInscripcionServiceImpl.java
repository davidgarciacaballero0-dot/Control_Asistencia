package com.universidad.academico.service.impl;

import com.universidad.academico.domain.BoletaInscripcion;
import com.universidad.academico.domain.Estudiante;
import com.universidad.academico.domain.Grupo;
import com.universidad.academico.dto.BoletaInscripcionDto;
import com.universidad.academico.dto.GrupoDto;
import com.universidad.academico.dto.VerificacionInscripcionDto;
import com.universidad.academico.repository.BoletaInscripcionRepository;
import com.universidad.academico.repository.EstudianteRepository;
import com.universidad.academico.repository.GrupoRepository;
import com.universidad.academico.service.BoletaInscripcionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementacion del servicio de Boletas de Inscripcion (CU06).
 */
@Service
@RequiredArgsConstructor
public class BoletaInscripcionServiceImpl implements BoletaInscripcionService {

    private final BoletaInscripcionRepository boletaRepository;
    private final EstudianteRepository estudianteRepository;
    private final GrupoRepository grupoRepository;

    @Override
    @Transactional
    public BoletaInscripcionDto inscribirEstudiante(BoletaInscripcionDto dto) {
        Estudiante estudiante = estudianteRepository.findById(dto.getEstudianteRegistro())
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado con registro: " + dto.getEstudianteRegistro()));

        Set<Grupo> grupos = new HashSet<>();
        for (Long grupoId : dto.getGrupoIds()) {
            Grupo grupo = grupoRepository.findById(grupoId)
                    .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado con ID: " + grupoId));
            grupos.add(grupo);
        }

        BoletaInscripcion boleta = BoletaInscripcion.builder()
                .fecha(dto.getFecha() != null ? dto.getFecha() : LocalDate.now())
                .hora(dto.getHora() != null ? dto.getHora() : LocalTime.now())
                .gestion(dto.getGestion())
                .estudiante(estudiante)
                .grupos(grupos)
                .build();

        BoletaInscripcion guardada = boletaRepository.save(boleta);
        return mapearADto(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public BoletaInscripcionDto obtenerPorNumero(Long numero) {
        return boletaRepository.findById(numero)
                .map(this::mapearADto)
                .orElseThrow(() -> new IllegalArgumentException("Boleta no encontrada con numero: " + numero));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoletaInscripcionDto> listarPorEstudiante(String registroEstudiante) {
        return boletaRepository.findByEstudianteRegistro(registroEstudiante).stream()
                .map(this::mapearADto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoletaInscripcionDto> listarTodas() {
        return boletaRepository.findAll().stream()
                .map(this::mapearADto)
                .toList();
    }

    @Override
    @Transactional
    public void anularInscripcion(Long numero) {
        if (!boletaRepository.existsById(numero)) {
            throw new IllegalArgumentException("Boleta no encontrada con numero: " + numero);
        }
        boletaRepository.deleteById(numero);
    }

    @Override
    @Transactional(readOnly = true)
    public VerificacionInscripcionDto verificarInscripcionEstudianteEnGrupo(String registroEstudiante, Long grupoId) {
        Estudiante estudiante = estudianteRepository.findById(registroEstudiante).orElse(null);
        if (estudiante == null) {
            return VerificacionInscripcionDto.builder()
                    .inscrito(false)
                    .registroEstudiante(registroEstudiante)
                    .mensaje("El estudiante no esta registrado en el sistema academico")
                    .build();
        }

        Grupo grupo = grupoRepository.findById(grupoId).orElse(null);
        if (grupo == null) {
            return VerificacionInscripcionDto.builder()
                    .inscrito(false)
                    .registroEstudiante(registroEstudiante)
                    .grupoId(grupoId)
                    .mensaje("El grupo academico especificado no existe")
                    .build();
        }

        boolean estaInscrito = boletaRepository.estaEstudianteInscritoEnGrupo(registroEstudiante, grupoId);

        if (!estaInscrito) {
            return VerificacionInscripcionDto.builder()
                    .inscrito(false)
                    .registroEstudiante(registroEstudiante)
                    .nombreEstudiante(estudiante.getNombre() + " " + estudiante.getApellidos())
                    .grupoId(grupoId)
                    .grupoNombre(grupo.getNombre())
                    .materiaSigla(grupo.getMateria().getSigla())
                    .materiaNombre(grupo.getMateria().getNombre())
                    .mensaje("El estudiante no se encuentra inscrito en este grupo academico")
                    .build();
        }

        return VerificacionInscripcionDto.builder()
                .inscrito(true)
                .registroEstudiante(registroEstudiante)
                .nombreEstudiante(estudiante.getNombre() + " " + estudiante.getApellidos())
                .grupoId(grupoId)
                .grupoNombre(grupo.getNombre())
                .materiaSigla(grupo.getMateria().getSigla())
                .materiaNombre(grupo.getMateria().getNombre())
                .mensaje("Inscripcion verificada y valida")
                .build();
    }

    private BoletaInscripcionDto mapearADto(BoletaInscripcion b) {
        List<GrupoDto> gruposDetalle = b.getGrupos().stream().map(g -> GrupoDto.builder()
                .id(g.getId())
                .nombre(g.getNombre())
                .cupo(g.getCupo())
                .materiaSigla(g.getMateria().getSigla())
                .materiaNombre(g.getMateria().getNombre())
                .docenteCodigo(g.getDocente().getCodigo())
                .docenteNombreCompleto(g.getDocente().getNombre() + " " + g.getDocente().getApellidos())
                .build()).toList();

        Set<Long> grupoIds = new HashSet<>();
        b.getGrupos().forEach(g -> grupoIds.add(g.getId()));

        return BoletaInscripcionDto.builder()
                .numero(b.getNumero())
                .fecha(b.getFecha())
                .hora(b.getHora())
                .gestion(b.getGestion())
                .estudianteRegistro(b.getEstudiante().getRegistro())
                .estudianteNombreCompleto(b.getEstudiante().getNombre() + " " + b.getEstudiante().getApellidos())
                .grupoIds(grupoIds)
                .gruposDetalle(gruposDetalle)
                .build();
    }
}
