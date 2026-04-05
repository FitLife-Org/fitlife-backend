package com.fitlife.core.security;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    // Create function token
    String generateToken(UserDetails userDetails);

    // Add JwtService.java
    String extractUsername(String token);

    boolean isTokenValid(String token, UserDetails userDetails);
}
