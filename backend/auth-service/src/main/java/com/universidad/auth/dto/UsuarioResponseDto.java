package com.universidad.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Objeto de transferencia de datos para presentar la informacion de un usuario.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponseDto {

    private Long id;
    private String username;
    private String nombreCompleto;
    private String email;
    private String identificadorReferencia;
    private Boolean activo;
    private List<String> roles;
}
