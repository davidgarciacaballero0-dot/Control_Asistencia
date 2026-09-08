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
     * Procesa un archivo CSV o Excel (.xlsx) y opcionalmente un archivo PDF con fotos,
     * registra/actualiza a los estudiantes, extrae las fotografias asociandolas a cada perfil,
     * los inscribe en el grupo correspondiente y realiza sincronizacion con baja logica
     * para estudiantes que ya no figuran en la lista actualizada.
     *
     * @param grupoId ID del grupo academico al que se inscribiran los estudiantes.
     * @param archivo Archivo principal de datos (CSV o Excel).
     * @param archivoPdf Archivo opcional PDF con fotografias de los perfiles.
     * @return DTO con el resumen del procesamiento y sincronizacion.
     */
    CargaMasivaEstudiantesDto importarEstudiantesAGrupo(Long grupoId, MultipartFile archivo, MultipartFile archivoPdf);

    /**
     * Sobrecarga para mantener compatibilidad cuando unicamente se envia el archivo de datos.
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
