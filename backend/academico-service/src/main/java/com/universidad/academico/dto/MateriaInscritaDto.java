package com.universidad.academico.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * DTO que representa una materia en la que esta inscrito un estudiante con sus horarios.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MateriaInscritaDto {

    private Long grupoId;
    private String grupoNombre;
    private Integer cupo;
    private String materiaSigla;
    private String materiaNombre;
    private String docenteCodigo;
    private String docenteNombreCompleto;
    private String docenteCorreo;
    private List<HorarioDto> horarios;
}
