package com.universidad.academico.service;

import com.universidad.academico.dto.DocenteDto;

import java.util.List;

/**
 * Interfaz de servicio para la gestion de Docentes (CU02).
 */
public interface DocenteService {

    DocenteDto crear(DocenteDto dto);

    DocenteDto actualizar(String codigo, DocenteDto dto);

    void eliminar(String codigo);

    DocenteDto obtenerPorCodigo(String codigo);

    List<DocenteDto> listarTodos();
}
