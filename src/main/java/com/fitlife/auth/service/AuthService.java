package com.fitlife.auth.service;

import com.fitlife.auth.dto.request.*;
import com.fitlife.auth.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse googleLogin(GoogleLoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(LogoutRequest request);

    void logoutAll();

    void verifyEmail(String token);

    void resendVerificationEmail(
            ResendVerificationEmailRequest request
    );

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}