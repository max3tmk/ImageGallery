package com.innowise.gateway.filter;

import com.innowise.common.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthFilterTest {

    private JwtUtil jwtUtil;
    private JwtAuthFilter filter;

    @BeforeEach
    void setup() {
        jwtUtil = mock(JwtUtil.class);
        filter = new JwtAuthFilter(jwtUtil);
    }

    @Test
    void filter_shouldPassPublicPath() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/auth/login").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, ex -> Mono.empty()))
                .verifyComplete();
    }

    @Test
    void filter_shouldReturn401IfMissingAuth() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/images/1").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, ex -> Mono.empty()))
                .verifyComplete();

        assert exchange.getResponse().getStatusCode().value() == 401;
    }

    @Test
    void filter_shouldReturn401IfInvalidToken() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/images/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtUtil.extractUsername("invalid-token")).thenReturn(null);

        StepVerifier.create(filter.filter(exchange, ex -> Mono.empty()))
                .verifyComplete();

        assert exchange.getResponse().getStatusCode().value() == 401;
    }

    @Test
    void filter_shouldPropagateValidToken() {
        UUID userId = UUID.randomUUID();

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/images/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtUtil.extractUsername("valid-token")).thenReturn("user");
        when(jwtUtil.validateToken("valid-token", "user")).thenReturn(true);
        when(jwtUtil.extractUserId("valid-token")).thenReturn(userId);

        StepVerifier.create(filter.filter(exchange, ex -> {
                    String xUserId = ex.getRequest().getHeaders().getFirst("X-User-Id");
                    assert xUserId.equals(userId.toString());
                    return Mono.empty();
                }))
                .verifyComplete();
    }
}