package com.universidad.academico.controller;

import com.universidad.academico.dto.BoletaInscripcionDto;
import com.universidad.academico.dto.VerificacionInscripcionDto;
import com.universidad.academico.service.BoletaInscripcionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para el Caso de Uso CU06: Gestionar Boleta de Inscripcion
 * y validacion inter-servicios de estudiantes inscritos.
 */
@RestController
@RequestMapping("/api/v1/academico/boletas")
@RequiredArgsConstructor
public class BoletaInscripcionController {

    private final BoletaInscripcionService boletaService;

    @PostMapping
    public ResponseEntity<BoletaInscripcionDto> inscribirEstudiante(@Valid @RequestBody BoletaInscripcionDto dto) {
        BoletaInscripcionDto creada = boletaService.inscribirEstudiante(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @GetMapping("/{numero}")
    public ResponseEntity<BoletaInscripcionDto> obtenerPorNumero(@PathVariable Long numero) {
        BoletaInscripcionDto boleta = boletaService.obtenerPorNumero(numero);
        return ResponseEntity.ok(boleta);
    }

    @GetMapping("/estudiante/{registro}")
    public ResponseEntity<List<BoletaInscripcionDto>> listarPorEstudiante(@PathVariable String registro) {
        return ResponseEntity.ok(boletaService.listarPorEstudiante(registro));
    }

    @GetMapping("/estudiante/{registro}/materias")
    public ResponseEntity<List<com.universidad.academico.dto.MateriaInscritaDto>> listarMateriasPorEstudiante(@PathVariable String registro) {
        return ResponseEntity.ok(boletaService.listarMateriasInscritasPorEstudiante(registro));
    }

    @GetMapping("/estudiante/{registro}/clases-hoy")
    public ResponseEntity<List<com.universidad.academico.dto.ClaseHorarioDto>> listarClasesDeHoyPorEstudiante(@PathVariable String registro) {
        return ResponseEntity.ok(boletaService.listarClasesDeHoyPorEstudiante(registro));
    }

    @GetMapping("/estudiante/{registro}/horarios")
    public ResponseEntity<List<com.universidad.academico.dto.ClaseHorarioDto>> listarTodasLasClasesPorEstudiante(@PathVariable String registro) {
        return ResponseEntity.ok(boletaService.listarTodasLasClasesPorEstudiante(registro));
    }

    @GetMapping
    public ResponseEntity<List<BoletaInscripcionDto>> listarTodas() {
        return ResponseEntity.ok(boletaService.listarTodas());
    }

    @DeleteMapping("/{numero}")
    public ResponseEntity<Void> anularInscripcion(@PathVariable Long numero) {
        boletaService.anularInscripcion(numero);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint para verificacion de inscripcion inter-microservicio.
     * Consumido por el MS Asistencia a traves de OpenFeign.
     *
     * @param registroEstudiante Numero de registro del estudiante.
     * @param grupoId ID del grupo al que asiste.
     * @return ResponseEntity con la verificacion.
     */
    @GetMapping("/verificar")
    public ResponseEntity<VerificacionInscripcionDto> verificarInscripcion(
            @RequestParam("registroEstudiante") String registroEstudiante,
            @RequestParam("grupoId") Long grupoId
    ) {
        VerificacionInscripcionDto resultado = boletaService.verificarInscripcionEstudianteEnGrupo(registroEstudiante, grupoId);
        return ResponseEntity.ok(resultado);
    }
}
