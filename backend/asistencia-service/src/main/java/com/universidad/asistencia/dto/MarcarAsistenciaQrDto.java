package com.universidad.asistencia.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para la peticion de marcacion de asistencia mediante escaneo de codigo QR desde la app movil.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarcarAsistenciaQrDto {

    @NotBlank(message = "El registro del estudiante es obligatorio")
    private String registroEstudiante;

    @NotBlank(message = "El codigo QR escaneado es obligatorio")
    private String codigoQr;

    private String observacion;
}
