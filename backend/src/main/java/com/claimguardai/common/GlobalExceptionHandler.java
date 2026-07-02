package com.claimguardai.common;

import com.claimguardai.auth.AuthenticationFailedException;
import com.claimguardai.auth.InvalidRefreshTokenException;
import com.claimguardai.auth.UserAlreadyExistsException;
import com.claimguardai.claims.ClaimNotFoundException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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
                .sorted((left, right) -> left.getField().compareToIgnoreCase(right.getField()))
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

        List<ApiErrorDetail> details = extractMessageDetails(exception);
        ErrorResponse response = errorResponseFactory.build(
                HttpStatus.BAD_REQUEST,
                "Malformed JSON request body.",
                request,
                details);

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

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRefreshTokenException(
            InvalidRefreshTokenException exception,
            HttpServletRequest request) {

        ErrorResponse response = errorResponseFactory.build(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage(),
                request);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistsException(
            UserAlreadyExistsException exception,
            HttpServletRequest request) {

        ErrorResponse response = errorResponseFactory.build(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
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

        log.error("Unhandled exception for path {}", request.getRequestURI(), exception);
        ErrorResponse response = errorResponseFactory.build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred.",
                request);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private ApiErrorDetail toDetail(FieldError fieldError) {
        return new ApiErrorDetail(fieldError.getField(), fieldError.getDefaultMessage());
    }

    private List<ApiErrorDetail> extractMessageDetails(HttpMessageNotReadableException exception) {
        Throwable mostSpecificCause = exception.getMostSpecificCause();
        if (mostSpecificCause instanceof UnrecognizedPropertyException propertyException) {
            return List.of(new ApiErrorDetail(
                    propertyException.getPropertyName(),
                    "Unknown field is not allowed."));
        }
        if (mostSpecificCause instanceof InvalidFormatException invalidFormatException) {
            String fieldPath = invalidFormatException.getPath().stream()
                    .map(reference -> reference.getFieldName())
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.joining("."));
            if (!fieldPath.isBlank()) {
                return List.of(new ApiErrorDetail(
                        fieldPath,
                        "Value must match the expected type or enum value."));
            }
        }

        return List.of();
    }
}
