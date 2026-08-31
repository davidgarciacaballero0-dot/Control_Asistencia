package com.universidad.academico.service;

import com.universidad.academico.dto.CargaMasivaEstudiantesDto;
import com.universidad.academico.dto.EstudianteDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Servicio de aplicacion para la importacion masiva de estudiantes (CSV / Excel)
 * e inscripcion automatica a grupos academicos.
 */
public interface ImportacionEstudiantesService {

    /**
     * Procesa un archivo CSV o Excel (.xlsx), registra/actualiza a los estudiantes,
     * los inscribe en el grupo correspondiente y aprovisiona sus credenciales en auth-service.
     *
     * @param grupoId ID del grupo academico al que se inscribiran los estudiantes.
     * @param archivo Archivo CSV o Excel subido por el docente o administrador.
     * @return DTO con el resumen del procesamiento.
     */
    CargaMasivaEstudiantesDto importarEstudiantesAGrupo(Long grupoId, MultipartFile archivo);

    /**
     * Obtiene la lista de estudiantes actualmente inscritos en un grupo determinado.
     *
     * @param grupoId ID del grupo academico.
     * @return Lista de EstudianteDto inscritos en el grupo.
     */
    List<EstudianteDto> listarEstudiantesPorGrupo(Long grupoId);
}
