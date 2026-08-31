package com.universidad.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la creacion idempotente y automatica de cuentas de usuario desde otros microservicios.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProvisionarUsuarioDto {

    @NotBlank(message = "El nombre de usuario no puede estar vacio")
    private String username;

    @NotBlank(message = "La contrasena inicial no puede estar vacia")
    private String password;

    private String email;

    @NotBlank(message = "El nombre completo no puede estar vacio")
    private String nombreCompleto;

    private String ci;

    private String rol;

    private String identificadorReferencia;
}
