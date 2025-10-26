package com.innowise.auth.integration;
import com.innowise.auth.dto.AuthResponse;
import com.innowise.auth.dto.ErrorResponse;
import com.innowise.auth.dto.LoginRequest;
import com.innowise.auth.dto.RegisterRequest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(OutputCaptureExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthServiceIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private final String registerUrl = "/api/auth/register";
    private final String loginUrl = "/api/auth/login";
    private final String refreshUrl = "/api/auth/refresh";

    private ResponseEntity<?> register(String username, String email, String password, Class<?> responseType) {
        return restTemplate.postForEntity(registerUrl, new RegisterRequest(username, email, password), responseType);
    }

    private ResponseEntity<?> login(String username, String password, Class<?> responseType) {
        return restTemplate.postForEntity(loginUrl, new LoginRequest(username, password), responseType);
    }

    private ResponseEntity<?> refresh(String refreshToken, Class<?> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + refreshToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(refreshUrl, HttpMethod.POST, entity, responseType);
    }

    @Test
    @Order(1)
    void register_ShouldWork_WhenAllValid() {
        ResponseEntity<AuthResponse> response = (ResponseEntity<AuthResponse>) register("user1", "user1@example.com", "password123", AuthResponse.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody().getAccessToken());
        assertNotNull(response.getBody().getRefreshToken());
        assertNotNull(response.getBody().getUserId());
    }

    @Test
    @Order(2)
    void register_ShouldFail_WhenPasswordTooShort() {
        ResponseEntity<ErrorResponse> response = (ResponseEntity<ErrorResponse>) register("user2", "user2@example.com", "123", ErrorResponse.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("Password should be from 6 to 100 symbols length"));
    }

    @Test
    @Order(3)
    void register_ShouldFail_WhenUsernameAlreadyExists() {
        register("bob", "bob1@example.com", "password123", AuthResponse.class);
        ResponseEntity<ErrorResponse> response = (ResponseEntity<ErrorResponse>) register("bob", "bob2@example.com", "password123", ErrorResponse.class);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("Username already taken"));
    }

    @Test
    @Order(4)
    void register_ShouldFail_WhenEmailAlreadyExists() {
        register("bob3", "bob@example.com", "password123", AuthResponse.class);
        ResponseEntity<ErrorResponse> response = (ResponseEntity<ErrorResponse>) register("newuser", "bob@example.com", "password123", ErrorResponse.class);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("Email already taken"));
    }

    @Test
    @Order(5)
    void register_ShouldFail_WhenUsernameAndEmailAlreadyExist() {
        register("bob4", "bob4@example.com", "password123", AuthResponse.class);

        ResponseEntity<ErrorResponse> response = (ResponseEntity<ErrorResponse>) register("bob4", "bob4@example.com", "password123", ErrorResponse.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("Username already taken: bob4"));
    }

    @Test
    @Order(6)
    void login_ShouldWork_WhenCredentialsValid() {
        register("userlogin", "userlogin@example.com", "password123", AuthResponse.class);
        ResponseEntity<AuthResponse> response = (ResponseEntity<AuthResponse>) login("userlogin", "password123", AuthResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().getAccessToken());
        assertNotNull(response.getBody().getRefreshToken());
    }

    @Test
    @Order(7)
    void login_ShouldFail_WhenPasswordInvalid() {
        register("userwrongpass", "userwrongpass@example.com", "password123", AuthResponse.class);

        ResponseEntity<ErrorResponse> response = (ResponseEntity<ErrorResponse>) login("userwrongpass", "wrongpass", ErrorResponse.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("Invalid username or password"));
    }

    @Test
    @Order(8)
    void refresh_ShouldWork_WhenTokenValid() {
        register("userrefresh", "userrefresh@example.com", "password123", AuthResponse.class);
        AuthResponse loginResp = (AuthResponse) login("userrefresh", "password123", AuthResponse.class).getBody();

        assertNotNull(loginResp.getRefreshToken());

        ResponseEntity<AuthResponse> response = (ResponseEntity<AuthResponse>) refresh(loginResp.getRefreshToken(), AuthResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getAccessToken());
        assertNotNull(response.getBody().getRefreshToken());
    }

    @Test
    @Order(9)
    void refresh_ShouldFail_WhenTokenInvalid() {
        String invalidToken = "invalidtoken";

        ResponseEntity<ErrorResponse> response = (ResponseEntity<ErrorResponse>) refresh(invalidToken, ErrorResponse.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid refresh token", response.getBody().getMessage());
        assertEquals("/api/auth/refresh", response.getBody().getPath());
    }
}