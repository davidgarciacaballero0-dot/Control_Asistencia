package com.universidad.academico.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Objeto de transferencia de datos estandar para respuestas de error.
 * Unifica el formato de respuesta ante fallos y excepciones HTTP en el microservicio academico.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDto {

    /**
     * Marca de tiempo en la que ocurrio el error.
     */
    private LocalDateTime timestamp;

    /**
     * Codigo de estado HTTP numerico (ej. 400, 404, 500).
     */
    private int status;

    /**
     * Descripcion textual del estado HTTP o tipo de error.
     */
    private String error;

    /**
     * Mensaje detallado que explica la causa del error.
     */
    private String mensaje;

    /**
     * Ruta o URI del endpoint que origino la excepcion.
     */
    private String ruta;
}
