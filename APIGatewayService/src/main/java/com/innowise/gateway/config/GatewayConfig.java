package com.innowise.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth_route", r -> r.path("/api/auth/**")
                        .uri("http://localhost:8080"))
                .route("image_route", r -> r.path("/api/images/**", "/api/user/**")
                        .uri("http://localhost:8081"))
                .build();
    }
}