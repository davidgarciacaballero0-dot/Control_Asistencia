package com.universidad.academico.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.universidad.academico.domain.DiaSemana;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

/**
 * DTO que representa una clase programada con su franja horaria y datos academicos.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaseHorarioDto {

    private Long grupoId;
    private String grupoNombre;
    private String materiaSigla;
    private String materiaNombre;
    private String docenteCodigo;
    private String docenteNombreCompleto;

    private Long horarioId;
    private DiaSemana dia;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaInicio;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaFin;

    private Boolean esHoy;
    private Boolean enCurso;
    private Boolean concluida;
}
