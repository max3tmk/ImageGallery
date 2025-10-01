package com.innowise.auth.service;

import com.innowise.auth.dto.AuthResponse;
import com.innowise.auth.dto.LoginRequest;
import com.innowise.auth.dto.RegisterRequest;
import com.innowise.auth.entity.User;
import com.innowise.auth.repository.UserRepository;
import com.innowise.common.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken: " + request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already taken: " + request.getEmail());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user = userRepository.save(user);

        UUID userId = user.getId();
        String accessToken = jwtUtil.generateToken(user.getUsername(), userId);
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), userId);

        return new AuthResponse(accessToken, refreshToken);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        UUID userId = user.getId();
        String accessToken = jwtUtil.generateToken(user.getUsername(), userId);
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), userId);

        return new AuthResponse(accessToken, refreshToken);
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RuntimeException("Refresh token is missing");
        }

        String username = jwtUtil.extractUsername(refreshToken);
        if (!jwtUtil.validateToken(refreshToken, username)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        UUID userId = user.getId();
        String newAccessToken = jwtUtil.generateToken(username, userId);
        String newRefreshToken = jwtUtil.generateRefreshToken(username, userId);

        return new AuthResponse(newAccessToken, newRefreshToken);
    }
}