package com.universidad.academico.service;

import com.universidad.academico.dto.MateriaDto;

import java.util.List;

/**
 * Interfaz de servicio para la gestion de Materias (CU04).
 */
public interface MateriaService {

    MateriaDto crear(MateriaDto dto);

    MateriaDto actualizar(String sigla, MateriaDto dto);

    void eliminar(String sigla);

    MateriaDto obtenerPorSigla(String sigla);

    List<MateriaDto> listarTodas();
}
