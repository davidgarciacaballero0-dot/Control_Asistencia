package com.universidad.gateway.filter;

import com.universidad.gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Filtro de seguridad del API Gateway para interceptar peticiones entrantes,
 * validar el token JWT y propagar la identidad a los microservicios aguas abajo.
 */
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final RouteValidator routeValidator;
    private final JwtUtil jwtUtil;

    public AuthenticationFilter(RouteValidator routeValidator, JwtUtil jwtUtil) {
        super(Config.class);
        this.routeValidator = routeValidator;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (routeValidator.esProtegida.test(request)) {
                // 1. Verificar si contiene la cabecera Authorization
                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    return respuestaNoAutorizada(exchange, "Falta la cabecera Authorization");
                }

                String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    return respuestaNoAutorizada(exchange, "Formato de token invalido. Debe comenzar con Bearer");
                }

                String token = authHeader.substring(7);

                try {
                    // 2. Validar token
                    jwtUtil.validarToken(token);
                    Claims claims = jwtUtil.extraerClaims(token);

                    // 3. Mutar la peticion agregando cabeceras de contexto de usuario
                    @SuppressWarnings("unchecked")
                    List<String> roles = (List<String>) claims.get("roles", List.class);
                    String rolesStr = roles != null ? String.join(",", roles) : "";
                    String userRef = (String) claims.get("identificadorReferencia");

                    ServerHttpRequest requestMutada = exchange.getRequest().mutate()
                            .header("X-User-Name", claims.getSubject())
                            .header("X-User-Roles", rolesStr)
                            .header("X-User-Ref", userRef != null ? userRef : "")
                            .build();

                    return chain.filter(exchange.mutate().request(requestMutada).build());

                } catch (Exception e) {
                    return respuestaNoAutorizada(exchange, "Acceso no autorizado: Token invalido o expirado");
                }
            }

            return chain.filter(exchange);
        };
    }

    private Mono<Void> respuestaNoAutorizada(ServerWebExchange exchange, String mensaje) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    public static class Config {
        // Configuraciones adicionales del filtro si fueran necesarias
    }
}
