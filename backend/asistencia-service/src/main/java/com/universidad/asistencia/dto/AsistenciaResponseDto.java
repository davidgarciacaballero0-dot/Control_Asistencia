package com.universidad.asistencia.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.universidad.asistencia.domain.EstadoAsistencia;
import com.universidad.asistencia.domain.MetodoValidacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO para la respuesta de confirmacion de registro de asistencia.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsistenciaResponseDto {

    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaRegistro;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime horaRegistro;

    private MetodoValidacion metodoValidacion;
    private String registroEstudiante;
    private String nombreEstudiante;
    private Long sesionId;
    private Long grupoId;
    private String grupoNombre;
    private String materiaSigla;
    private String materiaNombre;
    private EstadoAsistencia estadoAsistencia;
    private String mensaje;
}
