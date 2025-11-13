package com.innowise.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Value("${AUTH_SERVICE_URL:http://localhost:8080}")
    private String authServiceUrl;

    @Value("${IMAGE_SERVICE_URL:http://localhost:8081}")
    private String imageServiceUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth_route", r -> r.path("/api/auth/**")
                        .uri(authServiceUrl))
                .route("image_route", r -> r.path("/api/images", "/api/images/**", "/api/user/**")
                        .uri(imageServiceUrl))
                .build();
    }
}