package com.universidad.asistencia.chain.handler;

import com.universidad.asistencia.chain.AbstractAsistenciaHandler;
import com.universidad.asistencia.chain.AsistenciaValidationContext;
import com.universidad.asistencia.domain.EstadoAsistencia;
import com.universidad.asistencia.domain.SesionClase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalTime;

/**
 * ConcreteHandler5 del patron Chain of Responsibility (CU07).
 * Calcula la puntualidad del estudiante comparando la hora de inicio de la sesion
 * con la hora del marcado, utilizando una tolerancia configurable en minutos.
 */
@Component
public class CalculoPuntualidadHandler extends AbstractAsistenciaHandler {

    public static final int TOLERANCIA_MINUTOS_POR_DEFECTO = 15;

    private final int toleranciaMinutos;

    @Autowired
    public CalculoPuntualidadHandler(
            @Value("${asistencia.tolerancia-minutos:15}") int toleranciaMinutos
    ) {
        this.toleranciaMinutos = toleranciaMinutos > 0 ? toleranciaMinutos : TOLERANCIA_MINUTOS_POR_DEFECTO;
    }

    public CalculoPuntualidadHandler() {
        this(TOLERANCIA_MINUTOS_POR_DEFECTO);
    }

    @Override
    public void handleRequest(AsistenciaValidationContext request) {
        SesionClase sesion = request.getSesionClase();
        LocalTime horaEvaluacion = request.getHoraEvaluacion() != null
                ? request.getHoraEvaluacion()
                : LocalTime.now();

        long minutosTranscurridos = Duration.between(sesion.getHoraInicio(), horaEvaluacion).toMinutes();
        EstadoAsistencia estadoCalculado = (minutosTranscurridos <= toleranciaMinutos)
                ? EstadoAsistencia.PRESENTE
                : EstadoAsistencia.ATRASO;

        request.setHoraEvaluacion(horaEvaluacion);
        request.setEstadoAsistenciaCalculado(estadoCalculado);
        pasarAlSiguiente(request);
    }
}

