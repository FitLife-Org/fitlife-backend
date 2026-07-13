package com.fitlife.auth.service.impl;

import com.fitlife.auth.config.AuthProperties;
import com.fitlife.auth.entity.EmailVerificationToken;
import com.fitlife.auth.repository.EmailVerificationTokenRepository;
import com.fitlife.auth.service.EmailVerificationService;
import com.fitlife.auth.service.MailService;
import com.fitlife.auth.util.AuthTokenUtils;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.user.entity.User;
import com.fitlife.user.enums.UserStatus;
import com.fitlife.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class EmailVerificationServiceImpl
        implements EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final MailService mailService;
    private final AuthProperties authProperties;

    @Override
    public void createAndSendVerificationToken(User user) {
        tokenRepository.deleteAllByUserIdAndUsedFalse(user.getId());

        String rawToken = AuthTokenUtils.generateSecureToken();
        String tokenHash = AuthTokenUtils.hashToken(rawToken);

        EmailVerificationToken verificationToken =
                EmailVerificationToken.builder()
                        .user(user)
                        .tokenHash(tokenHash)
                        .expiresAt(
                                LocalDateTime.now().plusHours(
                                        authProperties
                                                .getEmailVerificationExpirationHours()
                                )
                        )
                        .used(false)
                        .build();

        tokenRepository.save(verificationToken);

        String verificationLink =
                authProperties.getFrontendVerificationUrl()
                        + "?token="
                        + rawToken;

        mailService.sendVerificationEmail(
                user.getEmail(),
                user.getFullName(),
                verificationLink
        );
    }

    @Override
    public void verifyEmail(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new AppException(
                    ErrorCode.INVALID_EMAIL_VERIFICATION_TOKEN
            );
        }

        String tokenHash = AuthTokenUtils.hashToken(rawToken);

        EmailVerificationToken verificationToken =
                tokenRepository.findByTokenHash(tokenHash)
                        .orElseThrow(() ->
                                new AppException(
                                        ErrorCode.INVALID_EMAIL_VERIFICATION_TOKEN
                                )
                        );

        User user = verificationToken.getUser();

        /*
         * Nếu tài khoản đã verify thì xem là thành công.
         */
        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            return;
        }

        if (Boolean.TRUE.equals(verificationToken.getUsed())) {
            throw new AppException(
                    ErrorCode.EMAIL_VERIFICATION_TOKEN_USED
            );
        }

        if (verificationToken.getExpiresAt()
                .isBefore(LocalDateTime.now())) {
            throw new AppException(
                    ErrorCode.EMAIL_VERIFICATION_TOKEN_EXPIRED
            );
        }

        user.setEmailVerified(true);

        if (user.getStatus() == UserStatus.PENDING) {
            user.setStatus(UserStatus.ACTIVE);
        }

        verificationToken.setUsed(true);
        verificationToken.setUsedAt(LocalDateTime.now());

        userRepository.save(user);
        tokenRepository.save(verificationToken);
    }

    @Override
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElse(null);

        /*
         * Không để lộ email có tồn tại hay không.
         */
        if (user == null) {
            return;
        }

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            return;
        }

        createAndSendVerificationToken(user);
    }
}