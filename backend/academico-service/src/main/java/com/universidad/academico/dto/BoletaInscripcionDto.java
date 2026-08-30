package com.universidad.academico.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

/**
 * DTO para la creacion y consulta de Boleta de Inscripcion.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoletaInscripcionDto {

    private Long numero;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime hora;

    @NotBlank(message = "La gestion academica es obligatoria (ej: 1-2024)")
    private String gestion;

    @NotBlank(message = "El registro del estudiante es obligatorio")
    private String estudianteRegistro;

    private String estudianteNombreCompleto;

    @NotEmpty(message = "Debe inscribir al menos un grupo")
    private Set<Long> grupoIds;

    private List<GrupoDto> gruposDetalle;
}
