package com.universidad.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * DTO para la respuesta de validacion de token JWT utilizada por el API Gateway o clientes.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenValidationResponseDto {

    private boolean valid;
    private String username;
    private String identificadorReferencia;
    private List<String> roles;
    private String mensaje;
}
