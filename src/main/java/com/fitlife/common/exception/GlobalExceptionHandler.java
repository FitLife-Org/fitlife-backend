package com.fitlife.common.exception;

import com.fitlife.common.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
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
    public ResponseEntity<ApiResponse<Void>>
    handleAppException(
            AppException exception
    ) {
        ErrorCode errorCode =
                exception.getErrorCode();

        String message =
                exception.getMessage() == null
                        || exception.getMessage().isBlank()
                        ? errorCode.getMessage()
                        : exception.getMessage();

        ApiResponse<Void> response =
                ApiResponse.error(
                        errorCode.getCode(),
                        message
                );

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }

    @ExceptionHandler(
            MethodArgumentNotValidException.class
    )
    public ResponseEntity<ApiResponse<Map<String, String>>>
    handleValidationException(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> errors =
                new LinkedHashMap<>();

        exception.getBindingResult()
                .getAllErrors()
                .forEach(error -> {
                    String fieldName =
                            error instanceof FieldError fieldError
                                    ? fieldError.getField()
                                    : error.getObjectName();

                    errors.putIfAbsent(
                            fieldName,
                            error.getDefaultMessage()
                    );
                });

        ErrorCode errorCode =
                ErrorCode.VALIDATION_FAILED;

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

    @ExceptionHandler(
            ConstraintViolationException.class
    )
    public ResponseEntity<ApiResponse<Void>>
    handleConstraintViolation(
            ConstraintViolationException exception
    ) {
        ErrorCode errorCode =
                ErrorCode.VALIDATION_FAILED;

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(
                        ApiResponse.error(
                                errorCode.getCode(),
                                exception.getMessage()
                        )
                );
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiResponse<Void>>
    handleMalformedRequest(
            Exception exception
    ) {
        log.debug(
                "Malformed API request",
                exception
        );

        ErrorCode errorCode =
                ErrorCode.INVALID_REQUEST;

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(
                        ApiResponse.error(
                                errorCode.getCode(),
                                errorCode.getMessage()
                        )
                );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleBadCredentialsException(
            BadCredentialsException exception
    ) {
        ErrorCode errorCode =
                ErrorCode.INVALID_CREDENTIALS;

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(
                        ApiResponse.error(
                                errorCode.getCode(),
                                errorCode.getMessage()
                        )
                );
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleLockedException(
            LockedException exception
    ) {
        ErrorCode errorCode =
                ErrorCode.ACCOUNT_LOCKED;

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(
                        ApiResponse.error(
                                errorCode.getCode(),
                                errorCode.getMessage()
                        )
                );
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<Void>>
    handleDisabledException(
            DisabledException exception
    ) {
        ErrorCode errorCode =
                resolveDisabledErrorCode(exception);

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(
                        ApiResponse.error(
                                errorCode.getCode(),
                                errorCode.getMessage()
                        )
                );
    }

    @ExceptionHandler(
            HttpRequestMethodNotSupportedException.class
    )
    public ResponseEntity<ApiResponse<Void>>
    handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception
    ) {
        ErrorCode errorCode =
                ErrorCode.METHOD_NOT_SUPPORTED;

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(
                        ApiResponse.error(
                                errorCode.getCode(),
                                errorCode.getMessage()
                                        + ": "
                                        + exception.getMethod()
                        )
                );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>>
    handleGlobalException(
            Exception exception
    ) {
        log.error(
                "Unexpected system error",
                exception
        );

        ErrorCode errorCode =
                ErrorCode.UNCATEGORIZED_EXCEPTION;

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(
                        ApiResponse.error(
                                errorCode.getCode(),
                                errorCode.getMessage()
                        )
                );
    }

    private ErrorCode resolveDisabledErrorCode(
            DisabledException exception
    ) {
        String message = exception.getMessage();

        if (ErrorCode.ACCOUNT_DELETED
                .name()
                .equals(message)) {
            return ErrorCode.ACCOUNT_DELETED;
        }

        if (ErrorCode.ACCOUNT_INACTIVE
                .name()
                .equals(message)) {
            return ErrorCode.ACCOUNT_INACTIVE;
        }

        return ErrorCode.ACCOUNT_INACTIVE;
    }
}