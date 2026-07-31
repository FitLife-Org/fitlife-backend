package com.fitlife.common.exception;

import com.fitlife.common.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(
            AppException exception
    ) {
        ErrorCode errorCode = exception.getErrorCode();

        String message = exception.getMessage() == null
                || exception.getMessage().isBlank()
                ? errorCode.getMessage()
                : exception.getMessage();

        return buildErrorResponse(
                errorCode,
                message
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>>
    handleValidationException(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getAllErrors()
                .forEach(error -> {
                    String fieldName = error instanceof FieldError fieldError
                            ? fieldError.getField()
                            : error.getObjectName();

                    errors.putIfAbsent(
                            fieldName,
                            resolveMessage(
                                    error.getDefaultMessage(),
                                    ErrorCode.VALIDATION_FAILED.getMessage()
                            )
                    );
                });

        ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(
                        ApiResponse.error(
                                errorCode.getCode(),
                                errorCode.getMessage(),
                                errors
                        )
                );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>>
    handleConstraintViolation(
            ConstraintViolationException exception
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        for (ConstraintViolation<?> violation
                : exception.getConstraintViolations()) {
            String fieldName = extractLastPathNode(
                    violation.getPropertyPath().toString()
            );

            errors.putIfAbsent(
                    fieldName,
                    resolveMessage(
                            violation.getMessage(),
                            ErrorCode.VALIDATION_FAILED.getMessage()
                    )
            );
        }

        ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(
                        ApiResponse.error(
                                errorCode.getCode(),
                                errorCode.getMessage(),
                                errors
                        )
                );
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleMalformedRequest(
            Exception exception
    ) {
        log.debug(
                "Malformed API request: {}",
                exception.getMessage()
        );

        return buildErrorResponse(
                ErrorCode.INVALID_REQUEST
        );
    }

    @ExceptionHandler({
            BadCredentialsException.class,
            UsernameNotFoundException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(
            RuntimeException exception
    ) {
        log.debug(
                "Authentication failed: {}",
                exception.getClass().getSimpleName()
        );

        return buildErrorResponse(
                ErrorCode.INVALID_CREDENTIALS
        );
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiResponse<Void>> handleLockedException(
            LockedException exception
    ) {
        return buildErrorResponse(
                ErrorCode.ACCOUNT_LOCKED
        );
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<Void>> handleDisabledException(
            DisabledException exception
    ) {
        return buildErrorResponse(
                resolveDisabledErrorCode(exception)
        );
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthenticated(
            AuthenticationCredentialsNotFoundException exception
    ) {
        return buildErrorResponse(
                ErrorCode.UNAUTHENTICATED
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            AccessDeniedException exception
    ) {
        return buildErrorResponse(
                ErrorCode.FORBIDDEN
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception
    ) {
        ErrorCode errorCode = ErrorCode.METHOD_NOT_SUPPORTED;

        return buildErrorResponse(
                errorCode,
                errorCode.getMessage()
                        + ": "
                        + exception.getMethod()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(
            DataIntegrityViolationException exception
    ) {
        log.warn(
                "Database constraint violation: {}",
                exception.getMostSpecificCause().getMessage()
        );

        return buildErrorResponse(
                ErrorCode.INVALID_REQUEST,
                "Data conflicts with an existing record or database constraint"
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(
            Exception exception
    ) {
        log.error(
                "Unexpected system error",
                exception
        );

        return buildErrorResponse(
                ErrorCode.UNCATEGORIZED_EXCEPTION
        );
    }

    private ResponseEntity<ApiResponse<Void>> buildErrorResponse(
            ErrorCode errorCode
    ) {
        return buildErrorResponse(
                errorCode,
                errorCode.getMessage()
        );
    }

    private ResponseEntity<ApiResponse<Void>> buildErrorResponse(
            ErrorCode errorCode,
            String message
    ) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(
                        ApiResponse.error(
                                errorCode.getCode(),
                                resolveMessage(
                                        message,
                                        errorCode.getMessage()
                                )
                        )
                );
    }

    private ErrorCode resolveDisabledErrorCode(
            DisabledException exception
    ) {
        String message = exception.getMessage();

        if (ErrorCode.ACCOUNT_DELETED.name().equals(message)) {
            return ErrorCode.ACCOUNT_DELETED;
        }

        if (ErrorCode.EMAIL_NOT_VERIFIED.name().equals(message)) {
            return ErrorCode.EMAIL_NOT_VERIFIED;
        }

        if (ErrorCode.ACCOUNT_LOCKED.name().equals(message)) {
            return ErrorCode.ACCOUNT_LOCKED;
        }

        if (ErrorCode.ACCOUNT_INACTIVE.name().equals(message)) {
            return ErrorCode.ACCOUNT_INACTIVE;
        }

        return ErrorCode.ACCOUNT_INACTIVE;
    }

    private String extractLastPathNode(String propertyPath) {
        if (propertyPath == null || propertyPath.isBlank()) {
            return "request";
        }

        int lastDot = propertyPath.lastIndexOf('.');

        return lastDot >= 0
                ? propertyPath.substring(lastDot + 1)
                : propertyPath;
    }

    private String resolveMessage(
            String message,
            String defaultMessage
    ) {
        return message == null || message.isBlank()
                ? defaultMessage
                : message;
    }
}
