package com.universidad.academico.service;

import com.universidad.academico.domain.Docente;
import com.universidad.academico.domain.Estudiante;
import com.universidad.academico.domain.Grupo;
import com.universidad.academico.domain.Materia;
import com.universidad.academico.dto.VerificacionInscripcionDto;
import com.universidad.academico.repository.BoletaInscripcionRepository;
import com.universidad.academico.repository.EstudianteRepository;
import com.universidad.academico.repository.GrupoRepository;
import com.universidad.academico.service.impl.BoletaInscripcionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para la logica de negocio de verificacion de inscripciones en academico-service.
 */
@ExtendWith(MockitoExtension.class)
public class BoletaInscripcionServiceTest {

    @Mock
    private BoletaInscripcionRepository boletaRepository;

    @Mock
    private EstudianteRepository estudianteRepository;

    @Mock
    private GrupoRepository grupoRepository;

    @InjectMocks
    private BoletaInscripcionServiceImpl boletaService;

    private Estudiante estudiantePrueba;
    private Grupo grupoPrueba;

    @BeforeEach
    void setUp() {
        estudiantePrueba = Estudiante.builder()
                .registro("2024001")
                .nombre("Juan")
                .apellidos("Perez")
                .correo("juan@estudiante.edu")
                .carrera("Informatica")
                .plan("2020")
                .build();

        Materia materia = Materia.builder().sigla("INF412").nombre("Arquitectura de Software").build();
        Docente docente = Docente.builder().codigo("DOC-101").nombre("Roberto").apellidos("Mendoza").correo("roberto@u.edu").build();

        grupoPrueba = Grupo.builder()
                .id(1L)
                .nombre("SC")
                .cupo(40)
                .materia(materia)
                .docente(docente)
                .build();
    }

    @Test
    @DisplayName("Debe verificar exitosamente cuando el estudiante esta inscrito en el grupo")
    void verificarInscripcion_EstudianteInscrito_RetornaVerificado() {
        when(estudianteRepository.findById("2024001")).thenReturn(Optional.of(estudiantePrueba));
        when(grupoRepository.findById(1L)).thenReturn(Optional.of(grupoPrueba));
        when(boletaRepository.estaEstudianteInscritoEnGrupo("2024001", 1L)).thenReturn(true);

        VerificacionInscripcionDto resultado = boletaService.verificarInscripcionEstudianteEnGrupo("2024001", 1L);

        assertTrue(resultado.isInscrito());
        assertEquals("Juan Perez", resultado.getNombreEstudiante());
        assertEquals("INF412", resultado.getMateriaSigla());
        assertEquals("SC", resultado.getGrupoNombre());
    }

    @Test
    @DisplayName("Debe retornar false si el estudiante no esta inscrito en el grupo consultado")
    void verificarInscripcion_EstudianteNoInscrito_RetornaNoInscrito() {
        when(estudianteRepository.findById("2024001")).thenReturn(Optional.of(estudiantePrueba));
        when(grupoRepository.findById(1L)).thenReturn(Optional.of(grupoPrueba));
        when(boletaRepository.estaEstudianteInscritoEnGrupo("2024001", 1L)).thenReturn(false);

        VerificacionInscripcionDto resultado = boletaService.verificarInscripcionEstudianteEnGrupo("2024001", 1L);

        assertFalse(resultado.isInscrito());
        assertEquals("El estudiante no se encuentra inscrito en este grupo academico", resultado.getMensaje());
    }
}
