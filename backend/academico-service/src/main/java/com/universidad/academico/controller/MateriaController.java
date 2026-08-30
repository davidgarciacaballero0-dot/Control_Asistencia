package com.universidad.academico.controller;

import com.universidad.academico.dto.MateriaDto;
import com.universidad.academico.service.MateriaService;
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
 * Controlador REST para el Caso de Uso CU04: Gestionar Materia.
 */
@RestController
@RequestMapping("/api/v1/academico/materias")
@RequiredArgsConstructor
public class MateriaController {

    private final MateriaService materiaService;

    @PostMapping
    public ResponseEntity<MateriaDto> crear(@Valid @RequestBody MateriaDto dto) {
        MateriaDto creado = materiaService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{sigla}")
    public ResponseEntity<MateriaDto> actualizar(
            @PathVariable String sigla,
            @Valid @RequestBody MateriaDto dto
    ) {
        MateriaDto actualizado = materiaService.actualizar(sigla, dto);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{sigla}")
    public ResponseEntity<Void> eliminar(@PathVariable String sigla) {
        materiaService.eliminar(sigla);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{sigla}")
    public ResponseEntity<MateriaDto> obtenerPorSigla(@PathVariable String sigla) {
        MateriaDto materia = materiaService.obtenerPorSigla(sigla);
        return ResponseEntity.ok(materia);
    }

    @GetMapping
    public ResponseEntity<List<MateriaDto>> listar() {
        return ResponseEntity.ok(materiaService.listarTodas());
    }
}
