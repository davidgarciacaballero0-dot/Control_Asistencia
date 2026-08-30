package com.universidad.asistencia.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO que mapea la respuesta del microservicio academico (academico-service)
 * al consultar la validez de inscripcion de un estudiante.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificacionInscripcionResponse {

    private boolean inscrito;
    private String registroEstudiante;
    private String nombreEstudiante;
    private Long grupoId;
    private String grupoNombre;
    private String materiaSigla;
    private String materiaNombre;
    private String mensaje;
}
