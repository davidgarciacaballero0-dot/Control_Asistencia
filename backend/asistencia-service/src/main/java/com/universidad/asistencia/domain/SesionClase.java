package com.universidad.asistencia.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad de dominio que representa una Sesion de Clase abierta por un docente.
 * Mantiene desacoplamiento almacenando unicamente los IDs de referencia de horarios y grupos.
 */
@Entity
@Table(name = "sesion_clase")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SesionClase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin")
    private LocalTime horaFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 30, nullable = false)
    @Builder.Default
    private EstadoSesion estado = EstadoSesion.ACTIVA;

    @Column(name = "id_horario_referencia", nullable = false)
    private Long idHorarioReferencia;

    @Column(name = "id_grupo_referencia", nullable = false)
    private Long idGrupoReferencia;

    @Column(name = "codigo_docente_referencia", length = 50)
    private String codigoDocenteReferencia;

    @Column(name = "tema", length = 200)
    private String tema;

    /**
     * Token unico criptografico generado para el codigo QR de la sesion.
     */
    @Column(name = "codigo_qr_generado", length = 100, unique = true)
    private String codigoQrGenerado;

    /**
     * Fecha y hora limite hasta la cual el codigo QR es valido para escanear.
     */
    @Column(name = "expiracion_qr")
    private LocalDateTime expiracionQr;

    @OneToMany(mappedBy = "sesionClase", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("sesionClase")
    @Builder.Default
    private List<Asistencia> asistencias = new ArrayList<>();
}
