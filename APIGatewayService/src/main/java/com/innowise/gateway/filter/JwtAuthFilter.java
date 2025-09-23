package com.innowise.gateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    private final JwtUtil jwtUtil;

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (isPublicPath(exchange)) {
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange);
            }

            String token = authHeader.substring(7);

            if (!jwtUtil.validateToken(token)) {
                return onError(exchange);
            }

            String userId = jwtUtil.extractUserId(token);
            ServerWebExchange newExchange = exchange.mutate()
                    .request(exchange.getRequest().mutate()
                            .header("X-User-Id", userId)
                            .build())
                    .build();

            return chain.filter(newExchange);
        };
    }

    private boolean isPublicPath(ServerWebExchange exchange) {
        String path = exchange.getRequest().getURI().getPath();
        return path.startsWith("/api/auth/register") || path.startsWith("/api/auth/login");
    }

    private Mono<Void> onError(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
    }
}