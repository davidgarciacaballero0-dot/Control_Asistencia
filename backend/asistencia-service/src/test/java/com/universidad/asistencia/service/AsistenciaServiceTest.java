package com.universidad.asistencia.service;

import com.universidad.asistencia.client.AcademicoFeignClient;
import com.universidad.asistencia.client.dto.VerificacionInscripcionResponse;
import com.universidad.asistencia.domain.Asistencia;
import com.universidad.asistencia.domain.EstadoAsistencia;
import com.universidad.asistencia.domain.EstadoSesion;
import com.universidad.asistencia.domain.SesionClase;
import com.universidad.asistencia.dto.AsistenciaResponseDto;
import com.universidad.asistencia.dto.MarcarAsistenciaQrDto;
import com.universidad.asistencia.repository.AsistenciaRepository;
import com.universidad.asistencia.repository.SesionClaseRepository;
import com.universidad.asistencia.service.impl.AsistenciaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para AsistenciaService y validacion de reglas de negocio de asistencia.
 */
@ExtendWith(MockitoExtension.class)
public class AsistenciaServiceTest {

    @Mock
    private AsistenciaRepository asistenciaRepository;

    @Mock
    private SesionClaseRepository sesionRepository;

    @Mock
    private AcademicoFeignClient academicoClient;

    @InjectMocks
    private AsistenciaServiceImpl asistenciaService;

    private SesionClase sesionActiva;

    @BeforeEach
    void setUp() {
        sesionActiva = SesionClase.builder()
                .id(1L)
                .fecha(LocalDate.now())
                .horaInicio(LocalTime.now())
                .estado(EstadoSesion.ACTIVA)
                .idHorarioReferencia(1L)
                .idGrupoReferencia(1L)
                .codigoDocenteReferencia("DOC-101")
                .codigoQrGenerado("QR-1234567890ABCDEF")
                .expiracionQr(LocalDateTime.now().plusMinutes(15))
                .build();
    }

    @Test
    @DisplayName("Debe registrar asistencia exitosamente cuando el QR es valido y el estudiante esta inscrito")
    void registrarAsistenciaQr_Valido_RetornaAsistenciaConfirmada() {
        MarcarAsistenciaQrDto request = MarcarAsistenciaQrDto.builder()
                .registroEstudiante("2024001")
                .codigoQr("QR-1234567890ABCDEF")
                .build();

        VerificacionInscripcionResponse verificacionFeign = VerificacionInscripcionResponse.builder()
                .inscrito(true)
                .registroEstudiante("2024001")
                .nombreEstudiante("Juan Perez")
                .grupoId(1L)
                .grupoNombre("SC")
                .materiaSigla("INF412")
                .materiaNombre("Arquitectura de Software")
                .build();

        when(sesionRepository.findByCodigoQrGenerado("QR-1234567890ABCDEF")).thenReturn(Optional.of(sesionActiva));
        when(asistenciaRepository.existsBySesionClaseIdAndRegistroEstudiante(1L, "2024001")).thenReturn(false);
        when(academicoClient.verificarInscripcion("2024001", 1L)).thenReturn(verificacionFeign);
        when(asistenciaRepository.save(any(Asistencia.class))).thenAnswer(i -> {
            Asistencia a = i.getArgument(0);
            a.setId(100L);
            return a;
        });

        AsistenciaResponseDto resultado = asistenciaService.registrarAsistenciaQr(request);

        assertNotNull(resultado);
        assertEquals("2024001", resultado.getRegistroEstudiante());
        assertEquals(EstadoAsistencia.PRESENTE, resultado.getEstadoAsistencia());
        assertEquals("INF412", resultado.getMateriaSigla());
    }

    @Test
    @DisplayName("Debe rechazar marcacion si el estudiante ya marco asistencia previamente en la sesion")
    void registrarAsistenciaQr_Duplicado_LanzaExcepcion() {
        MarcarAsistenciaQrDto request = MarcarAsistenciaQrDto.builder()
                .registroEstudiante("2024001")
                .codigoQr("QR-1234567890ABCDEF")
                .build();

        when(sesionRepository.findByCodigoQrGenerado("QR-1234567890ABCDEF")).thenReturn(Optional.of(sesionActiva));
        when(asistenciaRepository.existsBySesionClaseIdAndRegistroEstudiante(1L, "2024001")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> asistenciaService.registrarAsistenciaQr(request));
    }
}
