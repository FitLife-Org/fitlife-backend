package com.fitlife.controller;

import com.fitlife.dto.ApiResponse;
import com.fitlife.dto.LoginRequest;
import com.fitlife.dto.LoginResponse;
import com.fitlife.dto.RegisterRequest;
import com.fitlife.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth") // Thêm prefix để đồng bộ với các API khác
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Các API xác thực và đăng ký")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse result = authService.login(request);

        return ResponseEntity.ok( // Trả về HTTP 200 thực tế
                ApiResponse.<LoginResponse>builder()
                        .code(200)
                        .message("Đăng nhập thành công")
                        .data(result)
                        .build()
        );
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        String result = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED) // Trả về HTTP 201 thực tế
                .body(ApiResponse.<String>builder()
                        .code(201)
                        .message("Đăng ký tài khoản thành công")
                        .data(result)
                        .build()
                );
    }
}