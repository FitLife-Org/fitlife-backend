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

    private final RefreshTokenRepository
            refreshTokenRepository;

    private final AuthProperties
            authProperties;

    @Override
    public String create(
            User user
    ) {
        validateUser(user);

        long expirationDays =
                authProperties
                        .getRefreshTokenExpirationDays();

        if (expirationDays <= 0) {
            throw new IllegalStateException(
                    "Refresh token expiration days must be greater than zero"
            );
        }

        String rawToken =
                AuthTokenUtils
                        .generateSecureToken();

        String tokenHash =
                AuthTokenUtils
                        .hashToken(rawToken);

        LocalDateTime now =
                LocalDateTime.now();

        RefreshToken refreshToken =
                RefreshToken
                        .builder()
                        .user(user)
                        .tokenHash(tokenHash)
                        .expiresAt(
                                now.plusDays(
                                        expirationDays
                                )
                        )
                        .revoked(false)
                        .revokedAt(null)
                        .build();

        refreshTokenRepository.save(
                refreshToken
        );

        return rawToken;
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshToken validate(
            String rawToken
    ) {
        String normalizedToken =
                normalizeRawToken(rawToken);

        String tokenHash =
                AuthTokenUtils
                        .hashToken(
                                normalizedToken
                        );

        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByTokenHash(
                                tokenHash
                        )
                        .orElseThrow(() ->
                                new AppException(
                                        ErrorCode
                                                .INVALID_REFRESH_TOKEN
                                )
                        );

        if (Boolean.TRUE.equals(
                refreshToken.getRevoked()
        )) {
            throw new AppException(
                    ErrorCode
                            .REFRESH_TOKEN_REVOKED
            );
        }

        LocalDateTime expiresAt =
                refreshToken.getExpiresAt();

        if (
                expiresAt == null
                        || !expiresAt.isAfter(
                        LocalDateTime.now()
                )
        ) {
            throw new AppException(
                    ErrorCode
                            .REFRESH_TOKEN_EXPIRED
            );
        }

        if (
                refreshToken.getUser() == null
                        || refreshToken
                        .getUser()
                        .getId() == null
        ) {
            throw new AppException(
                    ErrorCode
                            .INVALID_REFRESH_TOKEN
            );
        }

        return refreshToken;
    }

    @Override
    public void revoke(
            String rawToken
    ) {
        if (
                rawToken == null
                        || rawToken.isBlank()
        ) {
            return;
        }

        String tokenHash =
                AuthTokenUtils
                        .hashToken(
                                rawToken.trim()
                        );

        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByTokenHash(
                                tokenHash
                        )
                        .orElse(null);

        /*
         * Logout idempotent:
         * token không tồn tại hoặc đã revoke vẫn coi là thành công.
         */
        if (
                refreshToken == null
                        || Boolean.TRUE.equals(
                        refreshToken.getRevoked()
                )
        ) {
            return;
        }

        refreshToken.setRevoked(true);

        refreshToken.setRevokedAt(
                LocalDateTime.now()
        );

        refreshTokenRepository.save(
                refreshToken
        );
    }

    @Override
    public void revokeAllByUserId(
            Long userId
    ) {
        if (userId == null) {
            throw new AppException(
                    ErrorCode.UNAUTHENTICATED
            );
        }

        refreshTokenRepository
                .revokeAllByUserId(
                        userId,
                        LocalDateTime.now()
                );
    }

    private String normalizeRawToken(
            String rawToken
    ) {
        if (
                rawToken == null
                        || rawToken.isBlank()
        ) {
            throw new AppException(
                    ErrorCode
                            .INVALID_REFRESH_TOKEN
            );
        }

        return rawToken.trim();
    }

    private void validateUser(
            User user
    ) {
        if (
                user == null
                        || user.getId() == null
        ) {
            throw new AppException(
                    ErrorCode
                            .INVALID_REFRESH_TOKEN
            );
        }
    }
}