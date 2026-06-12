package com.fitlife.auth.service;
import com.fitlife.auth.dto.LoginResponse;

public interface OAuth2Service {
    LoginResponse googleLogin(String token);
}