package com.universidad.gateway.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

/**
 * Validador de rutas abiertas (publicas) que no requieren validacion de token JWT.
 */
@Component
public class RouteValidator {

    public static final List<String> ENDPOINTS_ABIERTOS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/validate",
            "/actuator",
            "/v3/api-docs",
            "/swagger-ui"
    );

    public Predicate<ServerHttpRequest> esProtegida =
            request -> ENDPOINTS_ABIERTOS.stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));
}
