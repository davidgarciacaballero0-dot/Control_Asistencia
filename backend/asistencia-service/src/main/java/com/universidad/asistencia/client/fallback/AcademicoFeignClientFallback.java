package com.universidad.asistencia.client.fallback;

import com.universidad.asistencia.client.AcademicoFeignClient;
import com.universidad.asistencia.client.dto.VerificacionInscripcionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Componente de respaldo (Fallback) para el cliente OpenFeign de comunicacion con el microservicio academico.
 * Implementa el patron de degradacion controlada para mitigar el acoplamiento temporal si el servicio academico no responde.
 */
@Component
@Slf4j
public class AcademicoFeignClientFallback implements AcademicoFeignClient {

    /**
     * Respuesta por defecto ejecutada cuando el microservicio academico no se encuentra disponible.
     *
     * @param registroEstudiante Registro del estudiante.
     * @param grupoId ID del grupo academico.
     * @return VerificacionInscripcionResponse indicando no verificado por indisponibilidad del servicio.
     */
    @Override
    public VerificacionInscripcionResponse verificarInscripcion(String registroEstudiante, Long grupoId) {
        log.warn("Fallback ejecutado: el microservicio academico no responde al verificar estudiante: {} en grupo: {}",
                registroEstudiante, grupoId);

        return VerificacionInscripcionResponse.builder()
                .inscrito(false)
                .registroEstudiante(registroEstudiante)
                .grupoId(grupoId)
                .mensaje("El servicio academico no se encuentra disponible momentaneamente. No fue posible verificar la inscripcion.")
                .build();
    }
}
