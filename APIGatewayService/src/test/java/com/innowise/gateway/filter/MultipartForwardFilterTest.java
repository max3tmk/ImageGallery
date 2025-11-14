package com.innowise.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MultipartForwardFilterTest {

    @Test
    void filter_shouldNotProcessNonMultipart() {
        WebClient.Builder webClientBuilder = WebClient.builder();
        MultipartForwardFilter filter = new MultipartForwardFilter(webClientBuilder);

        MockServerHttpRequest request = MockServerHttpRequest.post("/api/images")
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Проверяем, что для не-multipart запросов фильтр пропускает запрос дальше
        boolean[] processed = {false};
        StepVerifier.create(filter.filter(exchange, ex -> {
                    processed[0] = true;
                    return Mono.empty();
                }))
                .verifyComplete();

        assertTrue(processed[0], "Filter should pass non-multipart requests through");
    }

    @Test
    void isMultipartRequest_shouldReturnTrueForMultipart() {
        WebClient.Builder webClientBuilder = WebClient.builder();
        MultipartForwardFilter filter = new MultipartForwardFilter(webClientBuilder);

        MockServerHttpRequest request = MockServerHttpRequest.post("/api/images")
                .header("Content-Type", MediaType.MULTIPART_FORM_DATA_VALUE)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // Используем reflection для вызова приватного метода
        try {
            java.lang.reflect.Method method = MultipartForwardFilter.class.getDeclaredMethod("isMultipartRequest", ServerWebExchange.class);
            method.setAccessible(true);
            Boolean result = (Boolean) method.invoke(filter, exchange);
            assertTrue(result, "Should detect multipart request");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void isMultipartRequest_shouldReturnFalseForNonMultipart() {
        WebClient.Builder webClientBuilder = WebClient.builder();
        MultipartForwardFilter filter = new MultipartForwardFilter(webClientBuilder);

        MockServerHttpRequest request = MockServerHttpRequest.post("/api/images")
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // Используем reflection для вызова приватного метода
        try {
            java.lang.reflect.Method method = MultipartForwardFilter.class.getDeclaredMethod("isMultipartRequest", ServerWebExchange.class);
            method.setAccessible(true);
            Boolean result = (Boolean) method.invoke(filter, exchange);
            assertFalse(result, "Should not detect non-multipart request as multipart");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void isMultipartRequest_shouldReturnFalseForNonPost() {
        WebClient.Builder webClientBuilder = WebClient.builder();
        MultipartForwardFilter filter = new MultipartForwardFilter(webClientBuilder);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/images")
                .header("Content-Type", MediaType.MULTIPART_FORM_DATA_VALUE)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // Используем reflection для вызова приватного метода
        try {
            java.lang.reflect.Method method = MultipartForwardFilter.class.getDeclaredMethod("isMultipartRequest", ServerWebExchange.class);
            method.setAccessible(true);
            Boolean result = (Boolean) method.invoke(filter, exchange);
            assertFalse(result, "Should not detect GET request as multipart even with multipart content type");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
