package com.claimguardai.common;

import com.claimguardai.auth.AuthenticationFailedException;
import com.claimguardai.claims.ClaimNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ApiErrorResponseFactory errorResponseFactory;

    public GlobalExceptionHandler(ApiErrorResponseFactory errorResponseFactory) {
        this.errorResponseFactory = errorResponseFactory;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        List<ApiErrorDetail> details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toDetail)
                .toList();

        ErrorResponse response = errorResponseFactory.build(
                HttpStatus.BAD_REQUEST,
                "Validation failed for request body.",
                request,
                details);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException exception,
            HttpServletRequest request) {

        ErrorResponse response = errorResponseFactory.build(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {

        ErrorResponse response = errorResponseFactory.build(
                HttpStatus.BAD_REQUEST,
                "Malformed JSON request body.",
                request);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationFailedException(
            AuthenticationFailedException exception,
            HttpServletRequest request) {

        ErrorResponse response = errorResponseFactory.build(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage(),
                request);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(ClaimNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleClaimNotFoundException(
            ClaimNotFoundException exception,
            HttpServletRequest request) {

        ErrorResponse response = errorResponseFactory.build(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException exception,
            HttpServletRequest request) {

        ErrorResponse response = errorResponseFactory.build(
                HttpStatus.FORBIDDEN,
                "You do not have permission to perform this action.",
                request);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception exception,
            HttpServletRequest request) {

        ErrorResponse response = errorResponseFactory.build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred.",
                request);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private ApiErrorDetail toDetail(FieldError fieldError) {
        return new ApiErrorDetail(fieldError.getField(), fieldError.getDefaultMessage());
    }
}
