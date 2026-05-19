package com.bookshop.shared.exception;


import com.bookshop.shared.dto.ErrorDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Catches validation errors triggered by <b>@Valid</b> annotations.
     * <br>
     * Aggregates field-specific errors into a clean, readable format
     * to be returned to the frontend.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorDetails errorDetails = new ErrorDetails(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                errors.toString(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorDetails> handleBadCredentialsException(
            HttpServletRequest request) {

        ErrorDetails errorDetails = new ErrorDetails(
                Instant.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "Authentication Failed",
                "Invalid username or password",
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Catches when registering already existing user with same email or username
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorDetails> handleUserAlreadyExistsException(
            Exception ex,
            HttpServletRequest request) {

        ErrorDetails errorDetails = new ErrorDetails(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "User already exists",
                ex.getMessage(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT);
    }


    /**
     * Catches JJWT library exceptions (Expired, Malformed, Bad Signature, etc.)
     */
    @ExceptionHandler(io.jsonwebtoken.JwtException.class)
    public ResponseEntity<ErrorDetails> handleJwtException(
            io.jsonwebtoken.JwtException ex,
            HttpServletRequest request) {

        ErrorDetails errorDetails = new ErrorDetails(
                Instant.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid or expired token",
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Catches Spring's automatic exception when the refreshToken cookie is completely missing
     */
    @ExceptionHandler(org.springframework.web.bind.MissingRequestCookieException.class)
    public ResponseEntity<ErrorDetails> handleMissingCookieException(
            org.springframework.web.bind.MissingRequestCookieException ex,
            HttpServletRequest request) {

        ErrorDetails errorDetails = new ErrorDetails(
                Instant.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "Missing required authentication cookie",
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Catches Spring Security Authorization failures (e.g., missing roles)
     * Thrown by @PreAuthorize annotations.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDetails> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request) {

        ErrorDetails errorDetails = new ErrorDetails(
                Instant.now(),
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                "You do not have permission to access this resource.",
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorDetails, HttpStatus.FORBIDDEN);
    }

    /**
     * Handles missing resources or hidden resources due to ownership violations.
     * Returns 404 Not Found.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        ErrorDetails errorDetails = new ErrorDetails(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                "Resource Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles IO failures during file uploads/processing.
     * Returns 500 Internal Server Error.
     */
    @ExceptionHandler(FileProcessingException.class)
    public ResponseEntity<ErrorDetails> handleFileProcessingException(
            FileProcessingException ex,
            HttpServletRequest request) {

        ErrorDetails errorDetails = new ErrorDetails(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "File Processing Error",
                ex.getMessage(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UnverifiedUserException.class)
    public ResponseEntity<ErrorDetails> handleUnverifiedAccount(UnverifiedUserException ex, HttpServletRequest request) {

        ErrorDetails errorDetails = new ErrorDetails(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "UNVERIFIED_ACCOUNT",
                ex.getMessage(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT);
    }
    /**
     * Catches Database constraints violations (e.g., Data Truncation, Out of range values, Unique constraints).
     *
     */
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ErrorDetails> handleDataIntegrityViolationException(
            org.springframework.dao.DataIntegrityViolationException ex,
            HttpServletRequest request) {

        log.error("Data Integrity Violation: {}", ex.getMessage());

        ErrorDetails errorDetails = new ErrorDetails(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Data Integrity Error",
                "Invalid data provided. Please ensure values  are within allowed limits.",
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }
    /**
     * Global Fallback: Catches any other exceptions to prevent messy stack traces.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(
            Exception ex,
            HttpServletRequest request) {


        log.error("Unhandled Exception at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        ErrorDetails errorDetails = new ErrorDetails(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred. Please try again later or contact support.",
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}