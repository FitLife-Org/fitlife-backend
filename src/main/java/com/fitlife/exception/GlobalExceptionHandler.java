package com.fitlife.exception;

import com.fitlife.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. CATCHING VALIDATION ERROR (@Valid - Example: Empty name, wrong email format)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        // Return HTTP 400 with a body containing the field errors
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.<Map<String, String>>builder()
                        .code(400)
                        .message("Dữ liệu đầu vào không hợp lệ")
                        .data(errors)
                        .build()
        );
    }

    // 2. CATCHING AUTHENTICATION ERROR (VD: Wrong username or password)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<String>> handleBadCredentialsException(BadCredentialsException ex) {
        // Return HTTP 401 with a message indicating authentication failure
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.<String>builder()
                        .code(401)
                        .message("Tài khoản hoặc mật khẩu không chính xác!")
                        .data(null)
                        .build()
        );
    }

    // 3. CATCHING RUNTIME EXCEPTION (VD: Username already exists, or any other custom exception thrown from Service layer)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<String>> handleRuntimeException(RuntimeException ex) {
        // Return HTTP 400 with the exception message as the response body
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.<String>builder()
                        .code(400)
                        .message(ex.getMessage())
                        .data(null)
                        .build()
        );
    }
}