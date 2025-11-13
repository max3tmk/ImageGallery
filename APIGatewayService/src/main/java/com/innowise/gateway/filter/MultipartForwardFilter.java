package com.innowise.gateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class MultipartForwardFilter implements GlobalFilter, Ordered {

    private final WebClient webClient;

    @Value("${IMAGE_SERVICE_URL:http://localhost:8081}")
    private String imageServiceUrl;

    public MultipartForwardFilter(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        if (!isMultipartRequest(exchange)) {
            return chain.filter(exchange);
        }

        String targetUrl = imageServiceUrl + exchange.getRequest().getURI().getPath();

        return webClient.method(exchange.getRequest().getMethod())
                .uri(targetUrl)
                .headers(headers -> {
                    headers.putAll(exchange.getRequest().getHeaders());
                    headers.remove(HttpHeaders.HOST);
                })
                .body(BodyInserters.fromDataBuffers(exchange.getRequest().getBody()))
                .exchangeToMono(clientResponse -> {
                    exchange.getResponse().setStatusCode(clientResponse.statusCode());
                    clientResponse.headers().asHttpHeaders()
                            .forEach((key, value) -> exchange.getResponse().getHeaders().put(key, value));
                    return exchange.getResponse().writeWith(clientResponse.bodyToFlux(org.springframework.core.io.buffer.DataBuffer.class));
                });
    }

    private boolean isMultipartRequest(ServerWebExchange exchange) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        MediaType contentType = headers.getContentType();
        return HttpMethod.POST.equals(exchange.getRequest().getMethod())
                && contentType != null
                && contentType.isCompatibleWith(MediaType.MULTIPART_FORM_DATA);
    }

    @Override
    public int getOrder() {
        return 1;
    }
}