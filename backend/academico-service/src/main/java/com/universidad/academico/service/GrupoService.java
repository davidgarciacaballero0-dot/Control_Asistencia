package com.universidad.academico.service;

import com.universidad.academico.dto.GrupoDto;

import java.util.List;

/**
 * Interfaz de servicio para la gestion de Grupos academicos (CU05).
 */
public interface GrupoService {

    GrupoDto crear(GrupoDto dto);

    GrupoDto actualizar(Long id, GrupoDto dto);

    void eliminar(Long id);

    GrupoDto obtenerPorId(Long id);

    List<GrupoDto> listarTodos();

    List<GrupoDto> listarPorMateria(String siglaMateria);

    List<GrupoDto> listarPorDocente(String codigoDocente);
}
