package com.shifter.freight_service.exceptions;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 🛑 Handle validation errors (missing fields, invalid data)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        String logMessage = "Validation Error: " + errors;
        log.error(logMessage, ex);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "Validation Failed",
                "details", errors.toString()
        ));
    }

    /**
     * 🔒 Handle authentication errors (bad credentials, access denied)
     */
    @ExceptionHandler({BadCredentialsException.class, AccessDeniedException.class})
    public ResponseEntity<Map<String, String>> handleAuthExceptions(Exception ex) {
        String logMessage = "Authentication Error: " + ex.getMessage();
        log.warn(logMessage, ex);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "error", "Authentication Failed",
                "message", ex.getMessage()
        ));
    }

    /**
     * 🛑 Handle JWT token errors (expired, malformed, invalid signature)
     */
    @ExceptionHandler({ExpiredJwtException.class, MalformedJwtException.class, JwtException.class})
    public ResponseEntity<Map<String, String>> handleJwtExceptions(Exception ex) {
        String logMessage = "JWT Error: " + ex.getMessage();
        log.warn(logMessage, ex);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "error", "Invalid Token",
                "message", ex.getMessage()
        ));
    }

    /**
     * 🔍 Handle entity errors (user/resource not found, already exists)
     */
    @ExceptionHandler({EntityNotFoundException.class, com.shifter.freight_service.exceptions.ResourceNotFoundException.class})
    public ResponseEntity<Map<String, String>> handleEntityExceptions(RuntimeException ex) {
        String logMessage = "Entity Error: " + ex.getMessage();
        log.warn(logMessage, ex);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", "Resource Not Found",
                "message", ex.getMessage()
        ));
    }

    /**
     * ⚠️ Handle general exceptions (NPE, IllegalArgumentException)
     */
    @ExceptionHandler({NullPointerException.class, IllegalArgumentException.class})
    public ResponseEntity<Map<String, String>> handleGeneralExceptions(Exception ex) {
        String logMessage = "General Error: " + ex.getMessage();
        log.error(logMessage, ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Internal Server Error",
                "message", ex.getMessage() != null ? ex.getMessage() : "Unexpected error occurred"
        ));
    }

    /**
     * 🌍 Handle unexpected exceptions (log actual error message instead of generic)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllExceptions(Exception ex) {
        log.error("Unexpected Error: " + ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "Unexpected Error",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleFileNotFoundExceptions(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Not Found", "message", ex.getMessage()));
    }

}
