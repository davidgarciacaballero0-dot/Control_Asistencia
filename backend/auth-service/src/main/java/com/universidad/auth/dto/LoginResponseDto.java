package com.universidad.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Objeto de transferencia de datos para la respuesta exitosa de inicio de sesion.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDto {

    private String token;
    private String tipo;
    private String username;
    private String nombreCompleto;
    private String email;
    private String identificadorReferencia;
    private List<String> roles;
}
