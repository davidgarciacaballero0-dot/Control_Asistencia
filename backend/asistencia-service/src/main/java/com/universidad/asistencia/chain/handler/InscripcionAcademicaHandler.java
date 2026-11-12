package com.universidad.asistencia.chain.handler;

import com.universidad.asistencia.chain.AbstractAsistenciaHandler;
import com.universidad.asistencia.chain.AsistenciaValidationContext;
import com.universidad.asistencia.client.AcademicoFeignClient;
import com.universidad.asistencia.client.dto.VerificacionInscripcionResponse;
import com.universidad.asistencia.domain.SesionClase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ConcreteHandler4 del patron Chain of Responsibility (CU07).
 * Invoca de manera desacoplada al microservicio academico a traves de OpenFeign
 * para verificar que el estudiante se encuentre formalmente inscrito en el grupo de la sesion.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InscripcionAcademicaHandler extends AbstractAsistenciaHandler {

    private final AcademicoFeignClient academicoClient;

    @Override
    public void handleRequest(AsistenciaValidationContext request) {
        SesionClase sesion = request.getSesionClase();
        String registroEstudiante = request.getRegistroEstudiante();

        VerificacionInscripcionResponse verificacion;
        try {
            verificacion = academicoClient.verificarInscripcion(registroEstudiante, sesion.getIdGrupoReferencia());
        } catch (Exception e) {
            log.error("Error al comunicarse con el microservicio academico a traves de OpenFeign: {}", e.getMessage());
            throw new IllegalStateException("No se pudo verificar la inscripcion academica. Servicio academico no disponible");
        }

        if (verificacion == null || !verificacion.isInscrito()) {
            String motivo = verificacion != null ? verificacion.getMensaje() : "Inscripcion no valida";
            throw new IllegalArgumentException("No se puede registrar asistencia: " + motivo);
        }

        request.setVerificacionInscripcion(verificacion);
        pasarAlSiguiente(request);
    }
}

