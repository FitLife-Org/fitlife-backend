package com.fitlife.auth.controller;

import com.fitlife.auth.dto.request.ForgotPasswordRequest;
import com.fitlife.auth.dto.request.LoginRequest;
import com.fitlife.auth.dto.request.RegisterRequest;
import com.fitlife.auth.dto.request.ResetPasswordRequest;
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
    @Operation(summary = "Request password reset token by email")
    public ApiResponse<Void> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        authService.forgotPassword(request);
        return ApiResponse.success("Password reset email sent successfully");
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using reset token")
    public ApiResponse<Void> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        authService.resetPassword(request);
        return ApiResponse.success("Password reset successfully");
    }
}