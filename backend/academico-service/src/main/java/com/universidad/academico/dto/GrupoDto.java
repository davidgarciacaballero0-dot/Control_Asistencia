package com.universidad.academico.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * DTO para transferencia de datos de Grupo con detalles de materia, docente y horarios.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrupoDto {

    private Long id;

    @NotBlank(message = "El nombre del grupo es obligatorio (ej: SC, SA)")
    private String nombre;

    @NotNull(message = "El cupo es obligatorio")
    @Min(value = 1, message = "El cupo minimo debe ser al menos 1")
    private Integer cupo;

    @NotBlank(message = "La sigla de la materia es obligatoria")
    private String materiaSigla;

    private String materiaNombre;

    @NotBlank(message = "El codigo del docente es obligatorio")
    private String docenteCodigo;

    private String docenteNombreCompleto;

    private List<HorarioDto> horarios;
}
