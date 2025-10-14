package com.innowise.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.auth.dto.AuthResponse;
import com.innowise.auth.dto.LoginRequest;
import com.innowise.auth.dto.RegisterRequest;
import com.innowise.auth.exception.GlobalExceptionHandler;
import com.innowise.auth.service.UserService;
import com.innowise.common.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler(messageSource);

        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(handler)
                .build();

        objectMapper = new ObjectMapper();
    }

    // ---------------- REGISTER ----------------

    @Test
    void register_ShouldReturnCreated_WhenValidRequest() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("john");
        request.setEmail("john@example.com");
        request.setPassword("password");

        AuthResponse response = new AuthResponse("access", "refresh", UUID.randomUUID().toString());
        when(userService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access"))
                .andExpect(jsonPath("$.refreshToken").value("refresh"));
    }

    @Test
    void register_ShouldReturnError_WhenServiceThrows() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("john");
        request.setEmail("john@example.com");
        request.setPassword("password");

        when(userService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Username already taken"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());
    }

    // ---------------- LOGIN ----------------

    @Test
    void login_ShouldReturnTokens_WhenValidCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("password");

        AuthResponse response = new AuthResponse("accessToken", "refreshToken", UUID.randomUUID().toString());
        when(userService.login(any(LoginRequest.class))).thenReturn(response);
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("accessToken"))
                .andExpect(jsonPath("$.refreshToken").value("refreshToken"));
    }

    @Test
    void login_ShouldReturnError_WhenInvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("wrong");

        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid username or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());
    }

    // ---------------- VALIDATE ----------------

    @Test
    void validateToken_ShouldReturnTrue_WhenTokenValid() throws Exception {
        String token = "Bearer validToken";

        when(jwtUtil.extractUsername("validToken")).thenReturn("john");
        when(jwtUtil.validateToken("validToken", "john")).thenReturn(true);

        mockMvc.perform(get("/api/auth/validate")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenInvalid() throws Exception {
        String token = "Bearer badToken";

        when(jwtUtil.extractUsername("badToken")).thenReturn("john");
        when(jwtUtil.validateToken("badToken", "john")).thenReturn(false);

        mockMvc.perform(get("/api/auth/validate")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false));
    }

    // ---------------- REFRESH ----------------

    @Test
    void refreshToken_ShouldReturnNewTokens_WhenValidRefreshToken() throws Exception {
        String token = "refreshToken";
        AuthResponse response = new AuthResponse("newAccess", "newRefresh", UUID.randomUUID().toString());

        when(userService.refreshToken(token)).thenReturn(response);

        mockMvc.perform(post("/api/auth/refresh")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("newAccess"))
                .andExpect(jsonPath("$.refreshToken").value("newRefresh"));
    }

    @Test
    void refreshToken_ShouldReturnError_WhenServiceThrows() throws Exception {
        String refreshToken = "invalid";

        when(userService.refreshToken(refreshToken))
                .thenThrow(new RuntimeException("Invalid token"));

        mockMvc.perform(post("/api/auth/refresh")
                        .header("Authorization", "Bearer invalid"))
                .andExpect(status().is5xxServerError());    }

    @Test
    void refreshToken_ShouldReturnError_WhenMissingHeader() throws Exception {
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Refresh token is missing"));
    }

    @Test
    void refreshToken_ShouldReturnError_WhenInvalidToken() throws Exception {
        String token = "invalid";

        when(userService.refreshToken(token)).thenThrow(new RuntimeException("Invalid refresh token"));

        mockMvc.perform(post("/api/auth/refresh")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.message").value("Invalid refresh token"));
    }
}