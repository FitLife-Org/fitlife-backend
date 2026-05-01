package com.fitlife.identity.service;
import com.fitlife.identity.dto.LoginResponse;

public interface OAuth2Service {
    LoginResponse googleLogin(String token);
}