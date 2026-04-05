package com.fitlife.core.exception;

import com.fitlife.core.response.ApiResponse;
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

    // 1. CATCHING VALIDATION ERROR (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            // Check safe after press style avoid ClassCastException
            String fieldName = (error instanceof FieldError) ? ((FieldError) error).getField() : error.getObjectName();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.<Map<String, String>>builder()
                        .code(400)
                        .message("Dữ liệu đầu vào không hợp lệ")
                        .data(errors)
                        .build()
        );
    }

    // 2. CATCHING AUTHENTICATION ERROR
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<String>> handleBadCredentialsException(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.<String>builder()
                        .code(401)
                        .message("Tài khoản hoặc mật khẩu không chính xác!")
                        .build()
        );
    }

    // 3. CATCHING CUSTOM BUSINESS EXCEPTION
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<String>> handleAppException(AppException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.<String>builder()
                        .code(400) // Hoặc lấy mã code từ chính AppException nếu bạn thiết kế linh hoạt hơn
                        .message(ex.getMessage())
                        .build()
        );
    }

    // 4. CATCH-ALL: Caught any unwanted system errors (NPE, DB Error...)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGlobalException(Exception ex) {
        // Gợi ý: Ghi log lỗi tại đây (log.error("Lỗi hệ thống: ", ex))
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.<String>builder()
                        .code(500)
                        .message("Đã có lỗi hệ thống xảy ra. Vui lòng thử lại sau!")
                        .build()
        );
    }
}