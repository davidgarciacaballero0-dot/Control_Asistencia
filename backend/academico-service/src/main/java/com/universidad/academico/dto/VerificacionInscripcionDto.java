package com.universidad.academico.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para responder a la verificacion cruzada de inscripcion desde el microservicio de asistencia.
 * Respeta el principio de bajo acoplamiento.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificacionInscripcionDto {

    private boolean inscrito;
    private String registroEstudiante;
    private String nombreEstudiante;
    private Long grupoId;
    private String grupoNombre;
    private String materiaSigla;
    private String materiaNombre;
    private String mensaje;
}
