package com.universidad.academico.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad de dominio que representa una Materia o Asignatura universitaria.
 */
@Entity
@Table(name = "materias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Materia {

    /**
     * Sigla unica de la materia (ej: INF412, ARQ101). Clave Primaria.
     */
    @Id
    @Column(name = "sigla", length = 20, nullable = false)
    private String sigla;

    @Column(name = "nombre", length = 150, nullable = false)
    private String nombre;
}
