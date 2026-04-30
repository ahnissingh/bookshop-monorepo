package com.bookshop.shared.exception;


import com.bookshop.shared.dto.ErrorDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
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


    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleUserNotFoundException(
            Exception ex,
            HttpServletRequest request) {

        ErrorDetails errorDetails = new ErrorDetails(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                "User not found",
                ex.getMessage(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
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
     * Global Fallback: Catches any other  exceptions to prevent messy stack traces.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(
            Exception ex,
            HttpServletRequest request) {

        ErrorDetails errorDetails = new ErrorDetails(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                ex.getMessage(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}