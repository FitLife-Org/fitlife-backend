package com.fitlife.auth.controller;

import com.fitlife.auth.dto.request.*;
import com.fitlife.auth.dto.response.AuthResponse;
import com.fitlife.auth.service.AuthService;
import com.fitlife.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs for register, login, and password reset")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register new member account")
    public ApiResponse<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        AuthResponse response = authService.register(request);
        return ApiResponse.created("Register successfully", response);
    }

    @PostMapping("/login")
    @Operation(summary = "Login by email or username")
    public ApiResponse<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse response = authService.login(request);
        return ApiResponse.success("Login successfully", response);
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ApiResponse.success("OTP has been sent to your email", null);
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.success("Password has been reset successfully", null);
    }

    @PostMapping("/google-login")
    @Operation(summary = "Login or register using Google ID token")
    public ApiResponse<AuthResponse> googleLogin(
            @Valid @RequestBody GoogleLoginRequest request
    ) {
        AuthResponse response = authService.googleLogin(request);
        return ApiResponse.success("Google login successfully", response);
    }
}