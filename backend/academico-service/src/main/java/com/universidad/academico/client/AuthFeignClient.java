package com.universidad.academico.client;

import com.universidad.academico.client.dto.ProvisionarUsuarioRequest;
import com.universidad.academico.client.fallback.AuthFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Cliente declarativo OpenFeign para comunicarse de forma desacoplada
 * con el microservicio de autenticacion (auth-service en puerto 8083).
 */
@FeignClient(
        name = "auth-service",
        url = "${servicios.auth.url:http://localhost:8083}",
        fallback = AuthFeignClientFallback.class
)
public interface AuthFeignClient {

    @PostMapping("/api/v1/auth/provisionar")
    Object provisionarUsuario(@RequestBody ProvisionarUsuarioRequest request);
}
