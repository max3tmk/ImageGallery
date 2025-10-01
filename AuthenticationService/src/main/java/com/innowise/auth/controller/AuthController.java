package com.innowise.auth.controller;

import com.innowise.auth.dto.AuthResponse;
import com.innowise.auth.dto.LoginRequest;
import com.innowise.auth.dto.RegisterRequest;
import com.innowise.auth.dto.ValidationResponse;
import com.innowise.auth.service.UserService;
import com.innowise.common.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        return ResponseEntity.ok(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @GetMapping("/validate")
    public ResponseEntity<ValidationResponse> validateToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);
        boolean isValid = jwtUtil.validateToken(token, username);
        return ResponseEntity.ok(new ValidationResponse(isValid));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        return ResponseEntity.ok(userService.refreshToken(token));
    }
}