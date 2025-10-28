package com.innowise.image.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "auth-service", url = "${auth.service.url:http://localhost:8080}")
public interface AuthServiceClient {

    @GetMapping("/api/auth/users/{id}/username")
    String getUsernameById(@PathVariable("id") UUID userId);
}