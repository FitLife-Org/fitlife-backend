package com.fitlife.auth.service.impl;

import com.fitlife.auth.config.AuthProperties;
import com.fitlife.auth.entity.RefreshToken;
import com.fitlife.auth.repository.RefreshTokenRepository;
import com.fitlife.auth.service.RefreshTokenService;
import com.fitlife.auth.util.AuthTokenUtils;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenServiceImpl
        implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthProperties authProperties;

    @Override
    public String create(User user) {
        String rawToken = AuthTokenUtils.generateSecureToken();
        String tokenHash = AuthTokenUtils.hashToken(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(
                        LocalDateTime.now().plusDays(
                                authProperties.getRefreshTokenExpirationDays()
                        )
                )
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshToken validate(String rawToken) {
        String tokenHash = AuthTokenUtils.hashToken(rawToken);

        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(() ->
                        new AppException(ErrorCode.INVALID_REFRESH_TOKEN)
                );

        if (Boolean.TRUE.equals(refreshToken.getRevoked())) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_REVOKED);
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        return refreshToken;
    }

    @Override
    public void revoke(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }

        String tokenHash = AuthTokenUtils.hashToken(rawToken);

        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenHash(tokenHash)
                .orElse(null);

        /*
         * Logout idempotent:
         * token không tồn tại vẫn trả thành công.
         */
        if (refreshToken == null) {
            return;
        }

        if (Boolean.TRUE.equals(refreshToken.getRevoked())) {
            return;
        }

        refreshToken.setRevoked(true);
        refreshToken.setRevokedAt(LocalDateTime.now());

        refreshTokenRepository.save(refreshToken);
    }

    @Override
    public void revokeAllByUserId(Long userId) {
        refreshTokenRepository.revokeAllByUserId(
                userId,
                LocalDateTime.now()
        );
    }
}