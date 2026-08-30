package com.universidad.academico.service;

import com.universidad.academico.dto.EstudianteDto;

import java.util.List;

/**
 * Interfaz de servicio para la gestion de Estudiantes (CU03).
 */
public interface EstudianteService {

    EstudianteDto crear(EstudianteDto dto);

    EstudianteDto actualizar(String registro, EstudianteDto dto);

    void eliminar(String registro);

    EstudianteDto obtenerPorRegistro(String registro);

    List<EstudianteDto> listarTodos();

    List<EstudianteDto> listarPorCarrera(String carrera);
}
