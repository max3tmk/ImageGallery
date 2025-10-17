package com.innowise.auth.exception;

import com.innowise.auth.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MessageSource messageSource = mock(MessageSource.class);
        handler = new GlobalExceptionHandler(messageSource);
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/auth/test");
    }

    @Test
    void handleValidationExceptions_shouldReturnBadRequest() {
        var target = new Object();
        var bindingResult = new BeanPropertyBindingResult(target, "object");
        bindingResult.addError(new FieldError("object", "username", "must not be blank"));
        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidationExceptions(ex, request);

        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().getMessage().contains("username"));
    }

    @Test
    void handleUserAlreadyExists_shouldReturnConflict() {
        UserAlreadyExistsException ex = new UserAlreadyExistsException("User already exists");
        ResponseEntity<ErrorResponse> response = handler.handleUserAlreadyExists(ex, request);

        assertEquals(409, response.getStatusCode().value());
        assertEquals("User already exists", response.getBody().getMessage());
    }

    @Test
    void handleInvalidCredentials_shouldReturnUnauthorized() {
        InvalidCredentialsException ex = new InvalidCredentialsException("Invalid credentials");
        ResponseEntity<ErrorResponse> response = handler.handleInvalidCredentials(ex, request);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("Invalid credentials", response.getBody().getMessage());
    }

    @Test
    void handleUserNotFound_shouldReturnNotFound() {
        UserNotFoundException ex = new UserNotFoundException("User not found");
        ResponseEntity<ErrorResponse> response = handler.handleUserNotFound(ex, request);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("User not found", response.getBody().getMessage());
    }

    @Test
    void handleRefreshToken_shouldReturnUnauthorized() {
        RefreshTokenException ex = new RefreshTokenException("Invalid or expired refresh token");
        ResponseEntity<ErrorResponse> response = handler.handleRefreshToken(ex, request);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("Invalid or expired refresh token", response.getBody().getMessage());
    }

    @Test
    void handleRuntime_shouldReturnInternalServerError() {
        RuntimeException ex = new RuntimeException("Unexpected error");
        ResponseEntity<ErrorResponse> response = handler.handleRuntime(ex, request);

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Unexpected error", response.getBody().getMessage());
        assertTrue(response.getBody().getTimestamp().isBefore(OffsetDateTime.now().plusSeconds(1)));
    }
}