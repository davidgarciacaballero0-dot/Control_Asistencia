package com.universidad.academico.controller;

import com.universidad.academico.dto.DocenteDto;
import com.universidad.academico.service.DocenteService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para el Caso de Uso CU02: Gestionar Docente.
 */
@RestController
@RequestMapping("/api/v1/academico/docentes")
@RequiredArgsConstructor
public class DocenteController {

    private final DocenteService docenteService;

    @PostMapping
    public ResponseEntity<DocenteDto> crear(@Valid @RequestBody DocenteDto dto) {
        DocenteDto creado = docenteService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<DocenteDto> actualizar(
            @PathVariable String codigo,
            @Valid @RequestBody DocenteDto dto
    ) {
        DocenteDto actualizado = docenteService.actualizar(codigo, dto);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> eliminar(@PathVariable String codigo) {
        docenteService.eliminar(codigo);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<DocenteDto> obtenerPorCodigo(@PathVariable String codigo) {
        DocenteDto docente = docenteService.obtenerPorCodigo(codigo);
        return ResponseEntity.ok(docente);
    }

    @GetMapping
    public ResponseEntity<List<DocenteDto>> listar() {
        return ResponseEntity.ok(docenteService.listarTodos());
    }
}
