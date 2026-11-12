package com.universidad.asistencia.chain.handler;

import com.universidad.asistencia.chain.AbstractAsistenciaHandler;
import com.universidad.asistencia.chain.AsistenciaValidationContext;
import com.universidad.asistencia.domain.SesionClase;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * ConcreteHandler2 del patron Chain of Responsibility (CU07).
 * Verifica que el token del codigo QR de la sesion activa no haya superado
 * su fecha y hora limite de expiracion.
 */
@Component
public class VigenciaQrHandler extends AbstractAsistenciaHandler {

    @Override
    public void handleRequest(AsistenciaValidationContext request) {
        SesionClase sesion = request.getSesionClase();
        if (sesion == null) {
            throw new IllegalStateException("No se ha resuelto una sesion de clase previa en la cadena de validacion");
        }

        if (sesion.getExpiracionQr() != null && LocalDateTime.now().isAfter(sesion.getExpiracionQr())) {
            throw new IllegalStateException(
                    "El codigo QR ha expirado. Solicite al docente que regenere el codigo");
        }

        pasarAlSiguiente(request);
    }
}

