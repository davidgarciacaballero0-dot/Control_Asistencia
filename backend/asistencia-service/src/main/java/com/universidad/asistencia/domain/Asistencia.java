package com.universidad.asistencia.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Entidad de dominio que representa el registro de Asistencia individual de un estudiante.
 */
@Entity
@Table(name = "asistencia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro;

    @Column(name = "hora_registro", nullable = false)
    private LocalTime horaRegistro;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_validacion", length = 30, nullable = false)
    private MetodoValidacion metodoValidacion;

    /**
     * Registro universitario del estudiante (clave logica hacia MS Academico).
     */
    @Column(name = "registro_estudiante", length = 30, nullable = false)
    private String registroEstudiante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sesion", nullable = false)
    @JsonIgnoreProperties("asistencias")
    private SesionClase sesionClase;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_asistencia", length = 30, nullable = false)
    @Builder.Default
    private EstadoAsistencia estadoAsistencia = EstadoAsistencia.PRESENTE;

    @Column(name = "observacion", length = 255)
    private String observacion;
}
