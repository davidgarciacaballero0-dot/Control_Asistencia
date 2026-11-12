package com.universidad.asistencia.chain.handler;

import com.universidad.asistencia.chain.AbstractAsistenciaHandler;
import com.universidad.asistencia.chain.AsistenciaValidationContext;
import com.universidad.asistencia.domain.SesionClase;
import com.universidad.asistencia.repository.AsistenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * ConcreteHandler3 del patron Chain of Responsibility (CU07).
 * Verifica que el estudiante no haya registrado previamente su asistencia
 * en la misma sesion de clase, evitando registros duplicados.
 */
@Component
@RequiredArgsConstructor
public class DuplicidadAsistenciaHandler extends AbstractAsistenciaHandler {

    private final AsistenciaRepository asistenciaRepository;

    @Override
    public void handleRequest(AsistenciaValidationContext request) {
        SesionClase sesion = request.getSesionClase();
        String registroEstudiante = request.getRegistroEstudiante();

        if (asistenciaRepository.existsBySesionClaseIdAndRegistroEstudiante(sesion.getId(), registroEstudiante)) {
            throw new IllegalStateException(
                    "El estudiante con registro " + registroEstudiante + " ya tiene registrada su asistencia en esta sesion");
        }

        pasarAlSiguiente(request);
    }
}

