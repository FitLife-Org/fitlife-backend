package com.fitlife.common.exception;

import com.fitlife.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j // Tá»± Ä‘á»™ng inject Logger
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. CATCHING VALIDATION ERROR (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = (error instanceof FieldError) ? ((FieldError) error).getField() : error.getObjectName();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        // CLEAN CODE: Truyá»n danh sĂ¡ch lá»—i vĂ o hĂ m error()
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, "Dá»¯ liá»‡u Ä‘áº§u vĂ o khĂ´ng há»£p lá»‡", errors));
    }

    // 2. CATCHING AUTHENTICATION ERROR
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<String>> handleBadCredentialsException(BadCredentialsException ex) {
        // CLEAN CODE
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(401, "TĂ i khoáº£n hoáº·c máº­t kháº©u khĂ´ng chĂ­nh xĂ¡c!"));
    }

    // 3. CATCHING CUSTOM BUSINESS EXCEPTION
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<String>> handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode(); // Láº¥y ErrorCode tá»« Exception

        // Láº¥y Ä‘Ăºng mĂ£ code tá»« Enum (vĂ­ dá»¥: 404, 400, 401) Ä‘á»™ng theo cáº¥u hĂ¬nh
        return ResponseEntity.status(errorCode.getCode())
                .body(ApiResponse.error(errorCode.getCode(), errorCode.getMessage()));
    }

    // 4. CATCH-ALL: Caught any unwanted system errors (NPE, DB Error...)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGlobalException(Exception ex) {
        // Ghi log lá»—i vĂ o file/console Ä‘á»ƒ Dev check, khĂ´ng tráº£ chi tiáº¿t cho Client trĂ¡nh lá»™ báº£o máº­t
        log.error("Lá»—i há»‡ thá»‘ng nghiĂªm trá»ng: ", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "ÄĂ£ cĂ³ lá»—i há»‡ thá»‘ng xáº£y ra. Vui lĂ²ng thá»­ láº¡i sau!"));
    }
}