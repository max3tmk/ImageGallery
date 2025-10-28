package com.innowise.auth.service;

import com.innowise.auth.dto.AuthResponse;
import com.innowise.auth.dto.LoginRequest;
import com.innowise.auth.dto.RegisterRequest;
import com.innowise.auth.entity.User;
import com.innowise.auth.exception.InvalidCredentialsException;
import com.innowise.auth.exception.RefreshTokenException;
import com.innowise.auth.exception.UserAlreadyExistsException;
import com.innowise.auth.exception.UserNotFoundException;
import com.innowise.auth.repository.UserRepository;
import com.innowise.common.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already taken: " + request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already taken: " + request.getEmail());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user = userRepository.save(user);

        UUID userId = user.getId();
        String accessToken = jwtUtil.generateToken(user.getUsername(), userId);
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), userId);

        return new AuthResponse(accessToken, refreshToken, userId.toString());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        UUID userId = user.getId();
        String accessToken = jwtUtil.generateToken(user.getUsername(), userId);
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), userId);

        return new AuthResponse(accessToken, refreshToken, userId.toString());
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RefreshTokenException("Refresh token is missing");
        }

        String username;
        try {
            username = jwtUtil.extractUsername(refreshToken);
        } catch (Exception e) {
            throw new RefreshTokenException("Invalid refresh token");
        }

        if (username == null || !jwtUtil.validateToken(refreshToken, username)) {
            throw new RefreshTokenException("Invalid refresh token");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        UUID userId = user.getId();
        String newAccessToken = jwtUtil.generateToken(username, userId);
        String newRefreshToken = jwtUtil.generateRefreshToken(username, userId);

        return new AuthResponse(newAccessToken, newRefreshToken, userId.toString());
    }

    public Optional<String> getUsernameById(UUID userId) {
        return userRepository.findById(userId)
                .map(User::getUsername);
    }
}