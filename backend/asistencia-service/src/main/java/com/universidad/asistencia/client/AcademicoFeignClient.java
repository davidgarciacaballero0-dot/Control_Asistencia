package com.universidad.asistencia.client;

import com.universidad.asistencia.client.dto.VerificacionInscripcionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Cliente declarativo OpenFeign para comunicarse de manera sincrona y desacoplada
 * con el microservicio academico (academico-service en puerto 8081).
 */
@FeignClient(name = "academico-service", url = "${servicios.academico.url:http://localhost:8081}")
public interface AcademicoFeignClient {

    /**
     * Consulta al microservicio academico si el estudiante esta inscrito en el grupo correspondiente.
     *
     * @param registroEstudiante Registro del estudiante.
     * @param grupoId ID del grupo academico.
     * @return VerificacionInscripcionResponse con el detalle de la consulta.
     */
    @GetMapping("/api/v1/academico/boletas/verificar")
    VerificacionInscripcionResponse verificarInscripcion(
            @RequestParam("registroEstudiante") String registroEstudiante,
            @RequestParam("grupoId") Long grupoId
    );
}
