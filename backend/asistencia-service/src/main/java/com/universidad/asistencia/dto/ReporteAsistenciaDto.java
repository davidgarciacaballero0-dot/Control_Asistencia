package com.universidad.asistencia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * DTO para resumen y reportes de asistencia de una sesion de clase o grupo academico.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteAsistenciaDto {

    private Long sesionId;
    private Long grupoId;
    private Integer totalPresentes;
    private Integer totalAtrasos;
    private Integer totalRegistrados;
    private List<AsistenciaResponseDto> detalleAsistencias;
}
