package com.innowise.auth.exception;

import com.innowise.auth.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String[] codes = error.getCodes();
            String message = null;

            if (codes != null) {
                for (String code : codes) {
                    message = messageSource.getMessage(
                            code,
                            null,
                            null,
                            LocaleContextHolder.getLocale()
                    );
                    if (message != null) break;
                }
            }

            if (message == null) {
                message = error.getDefaultMessage();
            }

            errors.put(fieldName, message);
        });

        String combinedMessage = errors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(combinedMessage)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        String message = ex.getMessage() != null ? ex.getMessage() : "Internal server error";

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        if (message.contains("already taken")) {
            status = HttpStatus.CONFLICT;
        } else if (message.contains("Invalid username or password") ||
                message.contains("Refresh token is missing") ||
                message.contains("Invalid or expired refresh token")) {
            status = HttpStatus.UNAUTHORIZED;
        } else if (message.contains("User not found")) {
            status = HttpStatus.NOT_FOUND;
        }

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(status).body(response);
    }
}