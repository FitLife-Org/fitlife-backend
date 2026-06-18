package com.fitlife.auth.service;

import com.fitlife.auth.dto.request.ForgotPasswordRequest;
import com.fitlife.auth.dto.request.LoginRequest;
import com.fitlife.auth.dto.request.RegisterRequest;
import com.fitlife.auth.dto.request.ResetPasswordRequest;
import com.fitlife.auth.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}