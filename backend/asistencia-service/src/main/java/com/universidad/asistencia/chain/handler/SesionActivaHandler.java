package com.universidad.asistencia.chain.handler;

import com.universidad.asistencia.chain.AbstractAsistenciaHandler;
import com.universidad.asistencia.chain.AsistenciaValidationContext;
import com.universidad.asistencia.domain.EstadoSesion;
import com.universidad.asistencia.domain.SesionClase;
import com.universidad.asistencia.repository.SesionClaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * ConcreteHandler1 del patron Chain of Responsibility (CU07).
 * Verifica que el codigo QR proporcionado corresponda a una SesionClase existente
 * y que dicha sesion se encuentre en estado ACTIVA.
 */
@Component
@RequiredArgsConstructor
public class SesionActivaHandler extends AbstractAsistenciaHandler {

    private final SesionClaseRepository sesionRepository;

    @Override
    public void handleRequest(AsistenciaValidationContext request) {
        SesionClase sesion = sesionRepository.findByCodigoQrGenerado(request.getCodigoQr())
                .orElseThrow(() -> new IllegalArgumentException(
                        "El codigo QR escaneado no corresponde a ninguna sesion valida"));

        if (sesion.getEstado() != EstadoSesion.ACTIVA) {
            throw new IllegalStateException(
                    "La sesion de clase se encuentra " + sesion.getEstado() + ". No es posible registrar asistencia");
        }

        request.setSesionClase(sesion);
        pasarAlSiguiente(request);
    }
}

