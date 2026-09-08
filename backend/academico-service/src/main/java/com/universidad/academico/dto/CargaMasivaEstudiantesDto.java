package com.universidad.academico.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO que resume el resultado de una importacion masiva de estudiantes (CSV o Excel).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CargaMasivaEstudiantesDto {

    private Long grupoId;
    private String grupoNombre;
    private String materiaSigla;
    private String materiaNombre;
    private int totalProcesados;
    private int totalNuevos;
    private int totalActualizados;
    private int totalInscritos;
    private int totalBajasLogicas;
    private int totalFotosProcesadas;
    private int totalFallidos;

    @Builder.Default
    private List<String> errores = new ArrayList<>();

    @Builder.Default
    private List<EstudianteDto> estudiantes = new ArrayList<>();
}
