package com.fitlife.auth.service;

import com.fitlife.auth.entity.RefreshToken;
import com.fitlife.user.entity.User;

public interface RefreshTokenService {

    String create(User user);

    RefreshToken validate(String rawToken);

    void revoke(String rawToken);

    void revokeAllByUserId(Long userId);
}