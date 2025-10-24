package com.innowise.auth.service;

import com.innowise.auth.dto.AuthResponse;
import com.innowise.auth.dto.LoginRequest;
import com.innowise.auth.dto.RegisterRequest;
import com.innowise.auth.entity.User;
import com.innowise.auth.repository.UserRepository;
import com.innowise.common.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("john");
        registerRequest.setEmail("john@example.com");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("john");
        loginRequest.setPassword("password123");

        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setPassword("encoded");
    }

    @Test
    void register_ShouldCreateUser_WhenValidData() {
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtil.generateToken(eq("john"), any(UUID.class))).thenReturn("access");
        when(jwtUtil.generateRefreshToken(eq("john"), any(UUID.class))).thenReturn("refresh");

        AuthResponse response = userService.register(registerRequest);

        assertEquals("access", response.getAccessToken());
        assertEquals("refresh", response.getRefreshToken());
        assertEquals(user.getId().toString(), response.getUserId());
    }

    @Test
    void register_ShouldThrowException_WhenUsernameExists() {
        when(userRepository.existsByUsername("john")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.register(registerRequest));

        assertTrue(ex.getMessage().contains("Username already taken"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_ShouldThrowException_WhenEmailExists() {
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.register(registerRequest));

        assertTrue(ex.getMessage().contains("Email already taken"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_ShouldReturnTokens_WhenCredentialsValid() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded")).thenReturn(true);
        when(jwtUtil.generateToken(eq("john"), any(UUID.class))).thenReturn("access");
        when(jwtUtil.generateRefreshToken(eq("john"), any(UUID.class))).thenReturn("refresh");

        AuthResponse response = userService.login(loginRequest);
        assertEquals("access", response.getAccessToken());
        assertEquals("refresh", response.getRefreshToken());
        assertEquals(user.getId().toString(), response.getUserId());
    }

    @Test
    void login_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.login(loginRequest));

        assertEquals("Invalid username or password", ex.getMessage());
    }

    @Test
    void login_ShouldThrowException_WhenPasswordInvalid() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.login(loginRequest));

        assertEquals("Invalid username or password", ex.getMessage());
    }

    @Test
    void refreshToken_ShouldGenerateNewTokens_WhenValid() {
        when(jwtUtil.extractUsername("refreshToken")).thenReturn("john");
        when(jwtUtil.validateToken("refreshToken", "john")).thenReturn(true);
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        when(jwtUtil.generateToken("john", user.getId())).thenReturn("newAccess");
        when(jwtUtil.generateRefreshToken("john", user.getId())).thenReturn("newRefresh");

        AuthResponse response = userService.refreshToken("refreshToken");

        assertEquals("newAccess", response.getAccessToken());
        assertEquals("newRefresh", response.getRefreshToken());
        assertEquals(user.getId().toString(), response.getUserId());
    }

    @Test
    void refreshToken_ShouldThrowException_WhenTokenMissing() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.refreshToken(""));

        assertEquals("Refresh token is missing", ex.getMessage());
    }

    @Test
    void refreshToken_ShouldThrowException_WhenTokenInvalid() {
        when(jwtUtil.extractUsername("badToken")).thenReturn("john");
        when(jwtUtil.validateToken("badToken", "john")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.refreshToken("badToken"));

        assertEquals("Invalid refresh token", ex.getMessage());
    }

    @Test
    void refreshToken_ShouldThrowException_WhenUserNotFound() {
        when(jwtUtil.extractUsername("refreshToken")).thenReturn("john");
        when(jwtUtil.validateToken("refreshToken", "john")).thenReturn(true);
        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.refreshToken("refreshToken"));

        assertTrue(ex.getMessage().contains("User not found"));
    }
}