package com.universidad.asistencia.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.universidad.asistencia.domain.EstadoSesion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * DTO para la respuesta con datos de una Sesion de Clase y su codigo QR activo.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SesionClaseDto {

    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime horaInicio;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime horaFin;

    private EstadoSesion estado;
    private Long idHorarioReferencia;
    private Long idGrupoReferencia;
    private String codigoDocenteReferencia;
    private String tema;
    private String codigoQrGenerado;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiracionQr;

    private Integer totalAsistenciasRegistradas;
}
