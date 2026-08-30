package com.universidad.academico.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para transferencia de datos de Materia.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MateriaDto {

    @NotBlank(message = "La sigla de la materia es obligatoria")
    private String sigla;

    @NotBlank(message = "El nombre de la materia es obligatorio")
    private String nombre;
}
