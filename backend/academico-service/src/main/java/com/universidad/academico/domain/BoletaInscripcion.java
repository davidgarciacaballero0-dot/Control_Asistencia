package com.universidad.academico.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad de dominio que representa la Boleta de Inscripcion de un Estudiante
 * en uno o multiples grupos para una gestion academica determinada.
 */
@Entity
@Table(name = "boleta_inscripcion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoletaInscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "numero")
    private Long numero;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "hora", nullable = false)
    private LocalTime hora;

    @Column(name = "gestion", length = 20, nullable = false)
    private String gestion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "registro_estudiante", nullable = false)
    private Estudiante estudiante;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "boleta_grupo",
            joinColumns = @JoinColumn(name = "numero_boleta"),
            inverseJoinColumns = @JoinColumn(name = "id_grupo")
    )
    @Builder.Default
    private Set<Grupo> grupos = new HashSet<>();
}
