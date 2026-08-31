package com.universidad.academico.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para enviar peticion de aprovisionamiento de usuario hacia auth-service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProvisionarUsuarioRequest {
    private String username;
    private String password;
    private String email;
    private String nombreCompleto;
    private String ci;
    private String rol;
    private String identificadorReferencia;
}
