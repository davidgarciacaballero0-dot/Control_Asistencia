package com.universidad.academico.controller;

import com.universidad.academico.dto.GrupoDto;
import com.universidad.academico.service.GrupoService;
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
 * Controlador REST para el Caso de Uso CU05: Gestionar Grupo.
 */
@RestController
@RequestMapping("/api/v1/academico/grupos")
@RequiredArgsConstructor
public class GrupoController {

    private final GrupoService grupoService;

    @PostMapping
    public ResponseEntity<GrupoDto> crear(@Valid @RequestBody GrupoDto dto) {
        GrupoDto creado = grupoService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GrupoDto> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody GrupoDto dto
    ) {
        GrupoDto actualizado = grupoService.actualizar(id, dto);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        grupoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<GrupoDto> obtenerPorId(@PathVariable Long id) {
        GrupoDto grupo = grupoService.obtenerPorId(id);
        return ResponseEntity.ok(grupo);
    }

    @GetMapping
    public ResponseEntity<List<GrupoDto>> listar(
            @RequestParam(required = false) String materia,
            @RequestParam(required = false) String docente
    ) {
        if (materia != null && !materia.isBlank()) {
            return ResponseEntity.ok(grupoService.listarPorMateria(materia));
        }
        if (docente != null && !docente.isBlank()) {
            return ResponseEntity.ok(grupoService.listarPorDocente(docente));
        }
        return ResponseEntity.ok(grupoService.listarTodos());
    }

    private final com.universidad.academico.service.ImportacionEstudiantesService importacionService;

    /**
     * Endpoint para la carga masiva de estudiantes mediante archivo CSV, Excel o PDF,
     * con soporte opcional para archivo PDF con fotografias de perfil y sincronizacion con baja logica.
     *
     * @param id ID del grupo academico.
     * @param archivo Archivo principal de estudiantes (CSV, Excel o PDF).
     * @param archivoPdf Archivo opcional PDF con fotografias de perfil.
     * @return DTO con el resumen de la importacion, inscripcion y bajas logicas.
     */
    @PostMapping(value = "/{id}/importar-estudiantes", consumes = {"multipart/form-data"})
    public ResponseEntity<com.universidad.academico.dto.CargaMasivaEstudiantesDto> importarEstudiantes(
            @PathVariable Long id,
            @RequestParam("archivo") org.springframework.web.multipart.MultipartFile archivo,
            @RequestParam(value = "archivoPdf", required = false) org.springframework.web.multipart.MultipartFile archivoPdf
    ) {
        com.universidad.academico.dto.CargaMasivaEstudiantesDto resultado = importacionService.importarEstudiantesAGrupo(id, archivo, archivoPdf);
        return ResponseEntity.ok(resultado);
    }

    /**
     * Endpoint para consultar la lista de estudiantes inscritos en un grupo determinado.
     *
     * @param id ID del grupo academico.
     * @return Lista de EstudianteDto inscritos en el grupo.
     */
    @GetMapping("/{id}/estudiantes")
    public ResponseEntity<List<com.universidad.academico.dto.EstudianteDto>> listarEstudiantesPorGrupo(@PathVariable Long id) {
        List<com.universidad.academico.dto.EstudianteDto> estudiantes = importacionService.listarEstudiantesPorGrupo(id);
        return ResponseEntity.ok(estudiantes);
    }
}
