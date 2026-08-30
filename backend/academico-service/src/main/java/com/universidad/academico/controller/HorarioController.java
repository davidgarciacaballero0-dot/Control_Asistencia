package com.universidad.academico.controller;

import com.universidad.academico.dto.HorarioDto;
import com.universidad.academico.service.HorarioService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para la gestion de Horarios de clases.
 */
@RestController
@RequestMapping("/api/v1/academico/horarios")
@RequiredArgsConstructor
public class HorarioController {

    private final HorarioService horarioService;

    @PostMapping("/grupo/{grupoId}")
    public ResponseEntity<HorarioDto> agregarHorarioAGrupo(
            @PathVariable Long grupoId,
            @Valid @RequestBody HorarioDto dto
    ) {
        HorarioDto creado = horarioService.agregarHorarioAGrupo(grupoId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @DeleteMapping("/{horarioId}")
    public ResponseEntity<Void> eliminar(@PathVariable Long horarioId) {
        horarioService.eliminar(horarioId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/grupo/{grupoId}")
    public ResponseEntity<List<HorarioDto>> listarPorGrupo(@PathVariable Long grupoId) {
        return ResponseEntity.ok(horarioService.listarPorGrupo(grupoId));
    }

    @GetMapping("/{horarioId}")
    public ResponseEntity<HorarioDto> obtenerPorId(@PathVariable Long horarioId) {
        return ResponseEntity.ok(horarioService.obtenerPorId(horarioId));
    }
}
