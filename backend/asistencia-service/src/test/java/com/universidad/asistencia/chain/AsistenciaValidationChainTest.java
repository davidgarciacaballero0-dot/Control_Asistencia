package com.universidad.asistencia.chain;

import com.universidad.asistencia.chain.handler.CalculoPuntualidadHandler;
import com.universidad.asistencia.chain.handler.DuplicidadAsistenciaHandler;
import com.universidad.asistencia.chain.handler.InscripcionAcademicaHandler;
import com.universidad.asistencia.chain.handler.SesionActivaHandler;
import com.universidad.asistencia.chain.handler.VigenciaQrHandler;
import com.universidad.asistencia.client.AcademicoFeignClient;
import com.universidad.asistencia.client.dto.VerificacionInscripcionResponse;
import com.universidad.asistencia.domain.EstadoAsistencia;
import com.universidad.asistencia.domain.EstadoSesion;
import com.universidad.asistencia.domain.SesionClase;
import com.universidad.asistencia.repository.AsistenciaRepository;
import com.universidad.asistencia.repository.SesionClaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del patron Chain of Responsibility (CU07: Gestionar Asistencia).
 * Verifica el encadenamiento de AbstractAsistenciaHandler y la ejecucion de handleRequest(request)
 * en cada uno de los 5 ConcreteHandlers.
 */
@ExtendWith(MockitoExtension.class)
class AsistenciaValidationChainTest {

    @Mock
    private SesionClaseRepository sesionRepository;

    @Mock
    private AsistenciaRepository asistenciaRepository;

    @Mock
    private AcademicoFeignClient academicoClient;

    private AbstractAsistenciaHandler cadenaRaiz;
    private SesionClase sesionActiva;

    @BeforeEach
    void setUp() {
        SesionActivaHandler h1 = new SesionActivaHandler(sesionRepository);
        VigenciaQrHandler h2 = new VigenciaQrHandler();
        DuplicidadAsistenciaHandler h3 = new DuplicidadAsistenciaHandler(asistenciaRepository);
        InscripcionAcademicaHandler h4 = new InscripcionAcademicaHandler(academicoClient);
        CalculoPuntualidadHandler h5 = new CalculoPuntualidadHandler(15);

        h1.setSiguiente(h2)
          .setSiguiente(h3)
          .setSiguiente(h4)
          .setSiguiente(h5);

        this.cadenaRaiz = h1;

        sesionActiva = SesionClase.builder()
                .id(10L)
                .fecha(LocalDate.now())
                .horaInicio(LocalTime.of(8, 0))
                .estado(EstadoSesion.ACTIVA)
                .idGrupoReferencia(5L)
                .codigoDocenteReferencia("DOC-101")
                .codigoQrGenerado("QR-VALIDO-123")
                .expiracionQr(LocalDateTime.now().plusMinutes(10))
                .build();
    }

    @Test
    @DisplayName("Debe completar toda la cadena y calcular estado PRESENTE dentro de los 15 minutos")
    void handleRequest_DentroDeTolerancia_AsignaEstadoPresente() {
        AsistenciaValidationContext request = AsistenciaValidationContext.builder()
                .registroEstudiante("2024001")
                .codigoQr("QR-VALIDO-123")
                .fechaEvaluacion(LocalDate.now())
                .horaEvaluacion(LocalTime.of(8, 10))
                .build();

        VerificacionInscripcionResponse verificacion = VerificacionInscripcionResponse.builder()
                .inscrito(true)
                .registroEstudiante("2024001")
                .nombreEstudiante("Juan Perez")
                .grupoId(5L)
                .grupoNombre("SC")
                .materiaSigla("INF412")
                .materiaNombre("Arquitectura de Software")
                .build();

        when(sesionRepository.findByCodigoQrGenerado("QR-VALIDO-123")).thenReturn(Optional.of(sesionActiva));
        when(asistenciaRepository.existsBySesionClaseIdAndRegistroEstudiante(10L, "2024001")).thenReturn(false);
        when(academicoClient.verificarInscripcion("2024001", 5L)).thenReturn(verificacion);

        cadenaRaiz.handleRequest(request);

        assertNotNull(request.getSesionClase());
        assertNotNull(request.getVerificacionInscripcion());
        assertEquals(EstadoAsistencia.PRESENTE, request.getEstadoAsistenciaCalculado());
    }

    @Test
    @DisplayName("Debe calcular estado ATRASO cuando supera los 15 minutos de tolerancia")
    void handleRequest_FueraDeTolerancia_AsignaEstadoAtraso() {
        AsistenciaValidationContext request = AsistenciaValidationContext.builder()
                .registroEstudiante("2024001")
                .codigoQr("QR-VALIDO-123")
                .fechaEvaluacion(LocalDate.now())
                .horaEvaluacion(LocalTime.of(8, 25))
                .build();

        VerificacionInscripcionResponse verificacion = VerificacionInscripcionResponse.builder()
                .inscrito(true)
                .registroEstudiante("2024001")
                .nombreEstudiante("Juan Perez")
                .grupoId(5L)
                .build();

        when(sesionRepository.findByCodigoQrGenerado("QR-VALIDO-123")).thenReturn(Optional.of(sesionActiva));
        when(asistenciaRepository.existsBySesionClaseIdAndRegistroEstudiante(10L, "2024001")).thenReturn(false);
        when(academicoClient.verificarInscripcion("2024001", 5L)).thenReturn(verificacion);

        cadenaRaiz.handleRequest(request);

        assertEquals(EstadoAsistencia.ATRASO, request.getEstadoAsistenciaCalculado());
    }

    @Test
    @DisplayName("Debe interrumpir la cadena en VigenciaQrHandler si el codigo QR ya expiro")
    void handleRequest_QrExpirado_LanzaIllegalStateException() {
        sesionActiva.setExpiracionQr(LocalDateTime.now().minusMinutes(1));

        AsistenciaValidationContext request = AsistenciaValidationContext.builder()
                .registroEstudiante("2024001")
                .codigoQr("QR-VALIDO-123")
                .build();

        when(sesionRepository.findByCodigoQrGenerado("QR-VALIDO-123")).thenReturn(Optional.of(sesionActiva));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> cadenaRaiz.handleRequest(request)
        );
        assertTrue(ex.getMessage().contains("expirado"));
    }

    @Test
    @DisplayName("Debe interrumpir la cadena en InscripcionAcademicaHandler si el estudiante no esta inscrito")
    void handleRequest_EstudianteNoInscrito_LanzaIllegalArgumentException() {
        AsistenciaValidationContext request = AsistenciaValidationContext.builder()
                .registroEstudiante("2024999")
                .codigoQr("QR-VALIDO-123")
                .build();

        VerificacionInscripcionResponse verificacionNoInscrito = VerificacionInscripcionResponse.builder()
                .inscrito(false)
                .mensaje("El estudiante no se encuentra inscrito en este grupo academico")
                .build();

        when(sesionRepository.findByCodigoQrGenerado("QR-VALIDO-123")).thenReturn(Optional.of(sesionActiva));
        when(asistenciaRepository.existsBySesionClaseIdAndRegistroEstudiante(10L, "2024999")).thenReturn(false);
        when(academicoClient.verificarInscripcion("2024999", 5L)).thenReturn(verificacionNoInscrito);

        assertThrows(IllegalArgumentException.class, () -> cadenaRaiz.handleRequest(request));
    }
}

