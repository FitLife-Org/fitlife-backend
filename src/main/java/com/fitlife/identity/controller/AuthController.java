package com.fitlife.identity.controller;

import com.fitlife.core.response.ApiResponse;
import com.fitlife.identity.dto.*;
import com.fitlife.identity.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Các API xác thực và đăng ký")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse result = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(result, "Đăng nhập thành công"));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        String result = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(result, "Đăng ký tài khoản thành công"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String result = authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(result, "Yêu cầu đặt lại mật khẩu đã được gửi thành công"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        String result = authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(result, "Đặt lại mật khẩu thành công"));
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<LoginResponse>> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        LoginResponse response = authService.googleLogin(request.getToken());
        return ResponseEntity.ok(ApiResponse.success(response, "Đăng nhập bằng Google thành công"));
    }
}