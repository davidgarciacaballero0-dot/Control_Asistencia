package com.universidad.asistencia.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para la solicitud de inicio de una sesion de clase por parte del docente.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IniciarSesionDto {

    @NotNull(message = "El ID del horario es obligatorio")
    private Long idHorarioReferencia;

    @NotNull(message = "El ID del grupo es obligatorio")
    private Long idGrupoReferencia;

    private String codigoDocenteReferencia;

    private String tema;

    /**
     * Minutos de validez del codigo QR generado (por defecto 15 minutos).
     */
    @Builder.Default
    private Integer minutosValidezQr = 15;
}
