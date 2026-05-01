package com.fitlife.identity.controller;

import com.fitlife.core.response.ApiResponse;
import com.fitlife.identity.dto.*;
import com.fitlife.identity.service.AuthService;
import com.fitlife.identity.service.OAuth2Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
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
@SecurityRequirements()
public class AuthController {

    private final AuthService authService;
    private final OAuth2Service oAuth2Service;

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập", description = "Xác thực tài khoản bằng username và password để nhận JWT token.")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse result = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(result, "Đăng nhập thành công"));
    }

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản", description = "Tạo mới tài khoản người dùng để đăng nhập vào hệ thống FitLife.")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        String result = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(result, "Đăng ký tài khoản thành công"));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Quên mật khẩu", description = "Gửi yêu cầu đặt lại mật khẩu tới email đã đăng ký.")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        String result = authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(result, "Yêu cầu đặt lại mật khẩu đã được gửi thành công"));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Đặt lại mật khẩu", description = "Xác nhận email, OTP và mật khẩu mới để hoàn tất quy trình reset password.")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        String result = authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(result, "Đặt lại mật khẩu thành công"));
    }

    @PostMapping("/google")
    @Operation(summary = "Đăng nhập bằng Google", description = "Đăng nhập bằng Google ID token để tạo hoặc xác thực tài khoản FitLife.")
    public ResponseEntity<ApiResponse<LoginResponse>> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        LoginResponse response = oAuth2Service.googleLogin(request.getToken());
        return ResponseEntity.ok(ApiResponse.success(response, "Đăng nhập bằng Google thành công"));
    }
}