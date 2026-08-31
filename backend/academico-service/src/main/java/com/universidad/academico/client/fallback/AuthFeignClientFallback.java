package com.universidad.academico.client.fallback;

import com.universidad.academico.client.AuthFeignClient;
import com.universidad.academico.client.dto.ProvisionarUsuarioRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Fallback para manejar caidas temporales o errores de conexion con auth-service.
 */
@Component
@Slf4j
public class AuthFeignClientFallback implements AuthFeignClient {

    @Override
    public Object provisionarUsuario(ProvisionarUsuarioRequest request) {
        log.warn("AuthFeignClient fallback activado: no se pudo provisionar usuario {} en auth-service (servicio no disponible)", request.getUsername());
        return null;
    }
}
