package com.universidad.asistencia.chain;

import com.universidad.asistencia.client.dto.VerificacionInscripcionResponse;
import com.universidad.asistencia.domain.EstadoAsistencia;
import com.universidad.asistencia.domain.SesionClase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Objeto Request / Contexto que transporta los datos de entrada y los resultados
 * parciales a lo largo de los eslabones del patron Chain of Responsibility
 * durante el proceso de registro de asistencia por Codigo QR (CU07).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsistenciaValidationContext {

    /**
     * Numero de registro universitario del estudiante que solicita marcar asistencia.
     */
    private String registroEstudiante;

    /**
     * Token del codigo QR escaneado o ingresado por el estudiante.
     */
    private String codigoQr;

    /**
     * Observacion opcional enviada desde el cliente movil o web.
     */
    private String observacion;

    /**
     * Fecha en la que se evalua la peticion de asistencia.
     */
    private LocalDate fechaEvaluacion;

    /**
     * Hora exacta en la que se evalua la peticion de asistencia.
     */
    private LocalTime horaEvaluacion;

    /**
     * Entidad SesionClase resuelta y validada por el primer eslabon (SesionActivaHandler).
     */
    private SesionClase sesionClase;

    /**
     * Respuesta de verificacion academica obtenida via OpenFeign por el cuarto eslabon (InscripcionAcademicaHandler).
     */
    private VerificacionInscripcionResponse verificacionInscripcion;

    /**
     * Estado de puntualidad (PRESENTE o ATRASO) determinado por el quinto eslabon (CalculoPuntualidadHandler).
     */
    private EstadoAsistencia estadoAsistenciaCalculado;
}

