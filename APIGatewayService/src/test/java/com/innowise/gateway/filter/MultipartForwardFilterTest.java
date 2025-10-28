package com.innowise.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class MultipartForwardFilterTest {

    private MultipartForwardFilter filter;
    private WebClient.Builder webClientBuilder;

    @BeforeEach
    void setup() {
        webClientBuilder = WebClient.builder();
        filter = new MultipartForwardFilter(webClientBuilder);
    }

    @Test
    void filter_shouldPassNonMultipartRequest() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/images")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, ex -> Mono.empty()))
                .verifyComplete();
    }

    @Test
    void filter_shouldDetectMultipart() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/images")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, ex -> Mono.empty()))
                .verifyComplete();
    }
}