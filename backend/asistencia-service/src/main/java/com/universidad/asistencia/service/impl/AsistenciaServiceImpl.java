package com.universidad.asistencia.service.impl;

import com.universidad.asistencia.chain.AbstractAsistenciaHandler;
import com.universidad.asistencia.chain.AsistenciaValidationContext;
import com.universidad.asistencia.chain.handler.CalculoPuntualidadHandler;
import com.universidad.asistencia.chain.handler.DuplicidadAsistenciaHandler;
import com.universidad.asistencia.chain.handler.InscripcionAcademicaHandler;
import com.universidad.asistencia.chain.handler.SesionActivaHandler;
import com.universidad.asistencia.chain.handler.VigenciaQrHandler;
import com.universidad.asistencia.client.AcademicoFeignClient;
import com.universidad.asistencia.client.dto.VerificacionInscripcionResponse;
import com.universidad.asistencia.domain.Asistencia;
import com.universidad.asistencia.domain.EstadoAsistencia;
import com.universidad.asistencia.domain.MetodoValidacion;
import com.universidad.asistencia.domain.SesionClase;
import com.universidad.asistencia.dto.AsistenciaResponseDto;
import com.universidad.asistencia.dto.MarcarAsistenciaQrDto;
import com.universidad.asistencia.dto.ReporteAsistenciaDto;
import com.universidad.asistencia.repository.AsistenciaRepository;
import com.universidad.asistencia.repository.SesionClaseRepository;
import com.universidad.asistencia.service.AsistenciaService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Implementacion de la logica de negocio de Asistencia (CU07).
 * Actua como el Client del patron de diseno Chain of Responsibility,
 * delegando la cadena de validaciones de negocio a la jerarquia AbstractAsistenciaHandler.
 */
@Service
@Slf4j
public class AsistenciaServiceImpl implements AsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final SesionClaseRepository sesionRepository;
    private final AcademicoFeignClient academicoClient;

    private final SesionActivaHandler sesionActivaHandler;
    private final VigenciaQrHandler vigenciaQrHandler;
    private final DuplicidadAsistenciaHandler duplicidadAsistenciaHandler;
    private final InscripcionAcademicaHandler inscripcionAcademicaHandler;
    private final CalculoPuntualidadHandler calculoPuntualidadHandler;

    /**
     * Referencia directa del Client hacia AbstractHandler segun el diagrama de referencia.
     */
    private AbstractAsistenciaHandler abstractHandler;

    /**
     * Constructor principal utilizado por Spring IoC para inyectar todos los componentes.
     */
    @Autowired
    public AsistenciaServiceImpl(
            AsistenciaRepository asistenciaRepository,
            SesionClaseRepository sesionRepository,
            AcademicoFeignClient academicoClient,
            SesionActivaHandler sesionActivaHandler,
            VigenciaQrHandler vigenciaQrHandler,
            DuplicidadAsistenciaHandler duplicidadAsistenciaHandler,
            InscripcionAcademicaHandler inscripcionAcademicaHandler,
            CalculoPuntualidadHandler calculoPuntualidadHandler
    ) {
        this.asistenciaRepository = asistenciaRepository;
        this.sesionRepository = sesionRepository;
        this.academicoClient = academicoClient;
        this.sesionActivaHandler = sesionActivaHandler;
        this.vigenciaQrHandler = vigenciaQrHandler;
        this.duplicidadAsistenciaHandler = duplicidadAsistenciaHandler;
        this.inscripcionAcademicaHandler = inscripcionAcademicaHandler;
        this.calculoPuntualidadHandler = calculoPuntualidadHandler;
        configurarCadenaResponsabilidad();
    }

    /**
     * Constructor secundario de compatibilidad para pruebas unitarias aisladas con mocks basicos.
     */
    public AsistenciaServiceImpl(
            AsistenciaRepository asistenciaRepository,
            SesionClaseRepository sesionRepository,
            AcademicoFeignClient academicoClient
    ) {
        this(
                asistenciaRepository,
                sesionRepository,
                academicoClient,
                new SesionActivaHandler(sesionRepository),
                new VigenciaQrHandler(),
                new DuplicidadAsistenciaHandler(asistenciaRepository),
                new InscripcionAcademicaHandler(academicoClient),
                new CalculoPuntualidadHandler()
        );
    }

    /**
     * Ensambla la cadena de responsabilidad enlazando secuencialmente los ConcreteHandlers.
     */
    @PostConstruct
    public final void configurarCadenaResponsabilidad() {
        AbstractAsistenciaHandler h1 = this.sesionActivaHandler != null
                ? this.sesionActivaHandler
                : new SesionActivaHandler(this.sesionRepository);
        AbstractAsistenciaHandler h2 = this.vigenciaQrHandler != null
                ? this.vigenciaQrHandler
                : new VigenciaQrHandler();
        AbstractAsistenciaHandler h3 = this.duplicidadAsistenciaHandler != null
                ? this.duplicidadAsistenciaHandler
                : new DuplicidadAsistenciaHandler(this.asistenciaRepository);
        AbstractAsistenciaHandler h4 = this.inscripcionAcademicaHandler != null
                ? this.inscripcionAcademicaHandler
                : new InscripcionAcademicaHandler(this.academicoClient);
        AbstractAsistenciaHandler h5 = this.calculoPuntualidadHandler != null
                ? this.calculoPuntualidadHandler
                : new CalculoPuntualidadHandler();

        h1.setSiguiente(h2)
          .setSiguiente(h3)
          .setSiguiente(h4)
          .setSiguiente(h5);

        this.abstractHandler = h1;
    }

    @Override
    @Transactional
    public AsistenciaResponseDto registrarAsistenciaQr(MarcarAsistenciaQrDto dto) {
        log.info("Procesando registro de asistencia QR para estudiante: {}, con codigo: {}",
                dto.getRegistroEstudiante(), dto.getCodigoQr());

        if (this.abstractHandler == null) {
            configurarCadenaResponsabilidad();
        }

        // 1. Construir el objeto Request (AsistenciaValidationContext) para la cadena
        AsistenciaValidationContext requestContext = AsistenciaValidationContext.builder()
                .registroEstudiante(dto.getRegistroEstudiante())
                .codigoQr(dto.getCodigoQr())
                .observacion(dto.getObservacion())
                .fechaEvaluacion(LocalDate.now())
                .horaEvaluacion(LocalTime.now())
                .build();

        // 2. Ejecutar la cadena de responsabilidad invocando handleRequest(request)
        this.abstractHandler.handleRequest(requestContext);

        // 3. Extraer resultados validados y enriquecidos por los ConcreteHandlers
        SesionClase sesion = requestContext.getSesionClase();
        VerificacionInscripcionResponse verificacion = requestContext.getVerificacionInscripcion();
        EstadoAsistencia estadoAsistencia = requestContext.getEstadoAsistenciaCalculado();

        // 4. Persistir el registro de asistencia en la base de datos db_asistencia
        Asistencia asistencia = Asistencia.builder()
                .fechaRegistro(requestContext.getFechaEvaluacion())
                .horaRegistro(requestContext.getHoraEvaluacion())
                .metodoValidacion(MetodoValidacion.QR)
                .registroEstudiante(dto.getRegistroEstudiante())
                .nombreEstudiante(verificacion.getNombreEstudiante())
                .sesionClase(sesion)
                .estadoAsistencia(estadoAsistencia)
                .observacion(dto.getObservacion() != null ? dto.getObservacion() : "Registro exitoso via escaneo QR")
                .build();

        Asistencia guardada = asistenciaRepository.save(asistencia);
        log.info("Asistencia registrada exitosamente con ID: {}, Estado: {}", guardada.getId(), estadoAsistencia);

        return mapearADtoConContextoAcademico(guardada, sesion, verificacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsistenciaResponseDto> listarPorSesion(Long sesionId) {
        return asistenciaRepository.findBySesionClaseId(sesionId).stream()
                .map(this::mapearADto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsistenciaResponseDto> listarPorEstudiante(String registroEstudiante) {
        return asistenciaRepository.findByRegistroEstudiante(registroEstudiante).stream()
                .map(this::mapearADto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteAsistenciaDto generarReporteSesion(Long sesionId) {
        SesionClase sesion = sesionRepository.findById(sesionId)
                .orElseThrow(() -> new IllegalArgumentException("Sesion no encontrada con ID: " + sesionId));

        List<Asistencia> asistencias = asistenciaRepository.findBySesionClaseId(sesionId);

        int totalPresentes = (int) asistencias.stream().filter(a -> a.getEstadoAsistencia() == EstadoAsistencia.PRESENTE).count();
        int totalAtrasos = (int) asistencias.stream().filter(a -> a.getEstadoAsistencia() == EstadoAsistencia.ATRASO).count();

        List<AsistenciaResponseDto> detalles = asistencias.stream()
                .map(this::mapearADto)
                .toList();

        return ReporteAsistenciaDto.builder()
                .sesionId(sesionId)
                .grupoId(sesion.getIdGrupoReferencia())
                .totalPresentes(totalPresentes)
                .totalAtrasos(totalAtrasos)
                .totalRegistrados(asistencias.size())
                .detalleAsistencias(detalles)
                .build();
    }

    private AsistenciaResponseDto mapearADtoConContextoAcademico(
            Asistencia guardada,
            SesionClase sesion,
            VerificacionInscripcionResponse verificacion
    ) {
        return AsistenciaResponseDto.builder()
                .id(guardada.getId())
                .fechaRegistro(guardada.getFechaRegistro())
                .horaRegistro(guardada.getHoraRegistro())
                .metodoValidacion(guardada.getMetodoValidacion())
                .registroEstudiante(guardada.getRegistroEstudiante())
                .nombreEstudiante(guardada.getNombreEstudiante())
                .sesionId(sesion.getId())
                .grupoId(sesion.getIdGrupoReferencia())
                .grupoNombre(verificacion != null ? verificacion.getGrupoNombre() : null)
                .materiaSigla(verificacion != null ? verificacion.getMateriaSigla() : null)
                .materiaNombre(verificacion != null ? verificacion.getMateriaNombre() : null)
                .estadoAsistencia(guardada.getEstadoAsistencia())
                .mensaje("Asistencia registrada exitosamente como " + guardada.getEstadoAsistencia())
                .build();
    }

    private AsistenciaResponseDto mapearADto(Asistencia a) {
        return AsistenciaResponseDto.builder()
                .id(a.getId())
                .fechaRegistro(a.getFechaRegistro())
                .horaRegistro(a.getHoraRegistro())
                .metodoValidacion(a.getMetodoValidacion())
                .registroEstudiante(a.getRegistroEstudiante())
                .nombreEstudiante(a.getNombreEstudiante() != null ? a.getNombreEstudiante() : a.getRegistroEstudiante())
                .sesionId(a.getSesionClase() != null ? a.getSesionClase().getId() : null)
                .grupoId(a.getSesionClase() != null ? a.getSesionClase().getIdGrupoReferencia() : null)
                .estadoAsistencia(a.getEstadoAsistencia())
                .mensaje("Registro de asistencia")
                .build();
    }
}
