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
@Tag(
        name = "Authentication",
        description = """
                APIs for register, login, email verification, \
                refresh token, logout and password reset
                """
)
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(
            summary = "Register new member account",
            description = """
                    Creates a PENDING member account and sends \
                    an email verification link.
                    """
    )
    public ApiResponse<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        AuthResponse response =
                authService.register(request);

        return ApiResponse.created(
                "Register successfully. Please verify your email.",
                response
        );
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(
            @Valid
            @RequestBody
            LoginRequest request
    ) {
        AuthResponse response =
                authService.login(request);

        return ApiResponse.success(
                "Login successfully",
                response
        );
    }

    @PostMapping("/google-login")
    @Operation(
            summary = "Login or register using Google ID token"
    )
    public ApiResponse<AuthResponse> googleLogin(
            @Valid @RequestBody GoogleLoginRequest request
    ) {
        AuthResponse response =
                authService.googleLogin(request);

        return ApiResponse.success(
                "Google login successfully",
                response
        );
    }

    @PostMapping("/refresh-token")
    public ApiResponse<AuthResponse> refreshToken(
            @Valid
            @RequestBody
            RefreshTokenRequest request
    ) {
        AuthResponse response =
                authService.refreshToken(request);

        return ApiResponse.success(
                "Refresh token successfully",
                response
        );
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @Valid
            @RequestBody
            LogoutRequest request
    ) {
        authService.logout(request);

        return ApiResponse.success(
                "Logout successfully",
                null
        );
    }

    @PostMapping("/logout-all")
    public ApiResponse<Void> logoutAll() {
        authService.logoutAll();

        return ApiResponse.success(
                "Logout all devices successfully",
                null
        );
    }

    @GetMapping("/verify-email")
    @Operation(
            summary = "Verify registered email"
    )
    public ApiResponse<Void> verifyEmail(
            @RequestParam String token
    ) {
        authService.verifyEmail(token);

        return ApiResponse.success(
                "Email verified successfully",
                null
        );
    }

    @PostMapping("/resend-verification")
    @Operation(
            summary = "Resend email verification link"
    )
    public ApiResponse<Void> resendVerificationEmail(
            @Valid
            @RequestBody
            ResendVerificationEmailRequest request
    ) {
        authService.resendVerificationEmail(
                request
        );

        return ApiResponse.success(
                "If the email exists and has not been verified, a verification email has been sent.",
                null
        );
    }

    @PostMapping("/forgot-password")
    @Operation(
            summary = "Send password reset OTP"
    )
    public ApiResponse<Void> forgotPassword(
            @Valid
            @RequestBody
            ForgotPasswordRequest request
    ) {
        authService.forgotPassword(request);

        return ApiResponse.success(
                "OTP has been sent to your email",
                null
        );
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Reset password using OTP"
    )
    public ApiResponse<Void> resetPassword(
            @Valid
            @RequestBody
            ResetPasswordRequest request
    ) {
        authService.resetPassword(request);

        return ApiResponse.success(
                "Password has been reset successfully",
                null
        );
    }
}