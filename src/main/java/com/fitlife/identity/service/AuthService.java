package com.fitlife.identity.service;

import com.fitlife.identity.dto.*;

public interface AuthService {

    // Logic Register: Create User + Member
    String register(RegisterRequest request);

    // Logic Login: Authenticate Spring Security + Generate JWT
    LoginResponse login(LoginRequest request);

    // Stream forgot password (Create OTP and send mail)
    String forgotPassword(ForgotPasswordRequest request);

    // Stream reset password (Check OTP and reset new password)
    String resetPassword(ResetPasswordRequest request);
}