package com.universidad.academico.controller;

import com.universidad.academico.dto.EstudianteDto;
import com.universidad.academico.service.EstudianteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para el Caso de Uso CU03: Gestionar Estudiante.
 */
@RestController
@RequestMapping("/api/v1/academico/estudiantes")
@RequiredArgsConstructor
public class EstudianteController {

    private final EstudianteService estudianteService;

    @PostMapping
    public ResponseEntity<EstudianteDto> crear(@Valid @RequestBody EstudianteDto dto) {
        EstudianteDto creado = estudianteService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{registro}")
    public ResponseEntity<EstudianteDto> actualizar(
            @PathVariable String registro,
            @Valid @RequestBody EstudianteDto dto
    ) {
        EstudianteDto actualizado = estudianteService.actualizar(registro, dto);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{registro}")
    public ResponseEntity<Void> eliminar(@PathVariable String registro) {
        estudianteService.eliminar(registro);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{registro}")
    public ResponseEntity<EstudianteDto> obtenerPorRegistro(@PathVariable String registro) {
        EstudianteDto estudiante = estudianteService.obtenerPorRegistro(registro);
        return ResponseEntity.ok(estudiante);
    }

    @GetMapping
    public ResponseEntity<List<EstudianteDto>> listar(
            @RequestParam(required = false) String carrera
    ) {
        if (carrera != null && !carrera.isBlank()) {
            return ResponseEntity.ok(estudianteService.listarPorCarrera(carrera));
        }
        return ResponseEntity.ok(estudianteService.listarTodos());
    }
}
