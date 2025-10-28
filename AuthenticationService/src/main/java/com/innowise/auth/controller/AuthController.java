package com.innowise.auth.controller;

import com.innowise.auth.dto.AuthResponse;
import com.innowise.auth.dto.LoginRequest;
import com.innowise.auth.dto.RegisterRequest;
import com.innowise.auth.dto.ValidationResponse;
import com.innowise.auth.exception.RefreshTokenException;
import com.innowise.auth.service.UserService;
import com.innowise.common.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @GetMapping("/validate")
    public ResponseEntity<ValidationResponse> validateToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        boolean isValid = jwtUtil.validateToken(token, jwtUtil.extractUsername(token));
        return ResponseEntity.ok(new ValidationResponse(isValid));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RefreshTokenException("Refresh token is missing");
        }
        return ResponseEntity.ok(userService.refreshToken(authHeader.substring(7)));
    }

    @GetMapping("/users/{id}/username")
    public ResponseEntity<String> getUsernameById(@PathVariable("id") UUID id) {
        return userService.getUsernameById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}