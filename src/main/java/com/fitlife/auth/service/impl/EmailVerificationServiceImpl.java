package com.fitlife.auth.service.impl;

import com.fitlife.auth.config.AuthProperties;
import com.fitlife.auth.entity.EmailVerificationToken;
import com.fitlife.auth.repository.EmailVerificationTokenRepository;
import com.fitlife.auth.service.EmailVerificationService;
import com.fitlife.auth.util.AuthTokenUtils;
import com.fitlife.common.exception.AppException;
import com.fitlife.common.exception.ErrorCode;
import com.fitlife.mail.service.EmailService;
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

    private static final String DEFAULT_DISPLAY_NAME =
            "bạn";

    private static final String VERIFICATION_EMAIL_SUBJECT =
            "Xác minh tài khoản FitLife";

    private final EmailVerificationTokenRepository
            tokenRepository;

    private final UserRepository
            userRepository;

    private final EmailService
            emailService;

    private final AuthProperties
            authProperties;

    @Override
    public void createAndSendVerificationToken(
            User user
    ) {
        validateUserForVerification(user);

        LocalDateTime now =
                LocalDateTime.now();

        /*
         * Mỗi user chỉ nên có một token chưa sử dụng.
         */
        tokenRepository
                .deleteAllByUserIdAndUsedFalse(
                        user.getId()
                );

        String rawToken =
                AuthTokenUtils
                        .generateSecureToken();

        String tokenHash =
                AuthTokenUtils
                        .hashToken(rawToken);

        EmailVerificationToken verificationToken =
                EmailVerificationToken
                        .builder()
                        .user(user)
                        .tokenHash(tokenHash)
                        .expiresAt(
                                now.plusHours(
                                        getExpirationHours()
                                )
                        )
                        .used(false)
                        .usedAt(null)
                        .build();

        tokenRepository.save(
                verificationToken
        );

        String verificationLink =
                buildVerificationLink(
                        rawToken
                );

        String displayName =
                resolveDisplayName(user);

        String htmlContent =
                buildVerificationEmail(
                        displayName,
                        verificationLink,
                        getExpirationHours()
                );

        emailService.sendHtmlMail(
                user.getEmail(),
                VERIFICATION_EMAIL_SUBJECT,
                htmlContent
        );
    }

    @Override
    public void verifyEmail(
            String rawToken
    ) {
        String normalizedToken =
                normalizeRawToken(rawToken);

        String tokenHash =
                AuthTokenUtils
                        .hashToken(
                                normalizedToken
                        );

        EmailVerificationToken verificationToken =
                tokenRepository
                        .findByTokenHash(
                                tokenHash
                        )
                        .orElseThrow(() ->
                                new AppException(
                                        ErrorCode
                                                .INVALID_EMAIL_VERIFICATION_TOKEN
                                )
                        );

        User user =
                verificationToken
                        .getUser();

        validateVerificationUser(user);

        /*
         * Verify email là operation idempotent.
         *
         * Khi user đã được xác minh, gọi lại endpoint vẫn được
         * coi là thành công thay vì trả lỗi.
         */
        if (Boolean.TRUE.equals(
                user.getEmailVerified()
        )) {
            return;
        }

        if (Boolean.TRUE.equals(
                verificationToken.getUsed()
        )) {
            throw new AppException(
                    ErrorCode
                            .EMAIL_VERIFICATION_TOKEN_USED
            );
        }

        LocalDateTime now =
                LocalDateTime.now();

        LocalDateTime expiresAt =
                verificationToken
                        .getExpiresAt();

        if (
                expiresAt == null
                        || !expiresAt.isAfter(now)
        ) {
            throw new AppException(
                    ErrorCode
                            .EMAIL_VERIFICATION_TOKEN_EXPIRED
            );
        }

        user.setEmailVerified(true);

        if (
                user.getStatus()
                        == UserStatus.PENDING
        ) {
            user.setStatus(
                    UserStatus.ACTIVE
            );
        }

        verificationToken.setUsed(true);
        verificationToken.setUsedAt(now);

        userRepository.save(user);

        tokenRepository.save(
                verificationToken
        );
    }

    @Override
    public void resendVerificationEmail(
            String email
    ) {
        String normalizedEmail =
                normalizeEmail(email);

        if (normalizedEmail == null) {
            return;
        }

        /*
         * Không ném USER_NOT_FOUND nhằm tránh lộ email có tồn tại
         * trong hệ thống hay không.
         */
        userRepository
                .findByEmail(
                        normalizedEmail
                )
                .filter(user ->
                        !Boolean.TRUE.equals(
                                user.getIsDeleted()
                        )
                )
                .filter(user ->
                        !Boolean.TRUE.equals(
                                user.getEmailVerified()
                        )
                )
                .filter(user ->
                        user.getStatus()
                                == UserStatus.PENDING
                )
                .ifPresent(
                        this::createAndSendVerificationToken
                );
    }

    private void validateUserForVerification(
            User user
    ) {
        if (
                user == null
                        || user.getId() == null
                        || user.getEmail() == null
                        || user.getEmail().isBlank()
        ) {
            throw new AppException(
                    ErrorCode.INVALID_REQUEST
            );
        }

        if (Boolean.TRUE.equals(
                user.getIsDeleted()
        )) {
            throw new AppException(
                    ErrorCode.ACCOUNT_DELETED
            );
        }

        /*
         * User đã xác minh không cần tạo thêm token.
         */
        if (Boolean.TRUE.equals(
                user.getEmailVerified()
        )) {
            return;
        }
    }

    private void validateVerificationUser(
            User user
    ) {
        if (
                user == null
                        || user.getId() == null
        ) {
            throw new AppException(
                    ErrorCode
                            .INVALID_EMAIL_VERIFICATION_TOKEN
            );
        }

        if (Boolean.TRUE.equals(
                user.getIsDeleted()
        )) {
            throw new AppException(
                    ErrorCode.ACCOUNT_DELETED
            );
        }
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
                            .INVALID_EMAIL_VERIFICATION_TOKEN
            );
        }

        return rawToken.trim();
    }

    private String normalizeEmail(
            String email
    ) {
        if (
                email == null
                        || email.isBlank()
        ) {
            return null;
        }

        return email
                .trim()
                .toLowerCase();
    }

    private String resolveDisplayName(
            User user
    ) {
        if (
                user.getFullName() == null
                        || user.getFullName().isBlank()
        ) {
            return DEFAULT_DISPLAY_NAME;
        }

        return user
                .getFullName()
                .trim();
    }

    private long getExpirationHours() {
        long expirationHours =
                authProperties
                        .getEmailVerificationExpirationHours();

        if (expirationHours <= 0) {
            throw new IllegalStateException(
                    "Email verification expiration hours must be greater than zero"
            );
        }

        return expirationHours;
    }

    private String buildVerificationLink(
            String rawToken
    ) {
        String frontendVerificationUrl =
                authProperties
                        .getFrontendVerificationUrl();

        if (
                frontendVerificationUrl == null
                        || frontendVerificationUrl.isBlank()
        ) {
            throw new IllegalStateException(
                    "Frontend verification URL is not configured"
            );
        }

        String separator =
                frontendVerificationUrl
                        .contains("?")
                        ? "&"
                        : "?";

        return frontendVerificationUrl
                .trim()
                + separator
                + "token="
                + rawToken;
    }

    private String buildVerificationEmail(
            String displayName,
            String verificationLink,
            long expirationHours
    ) {
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport"
                          content="width=device-width, initial-scale=1.0">
                </head>

                <body style="
                    margin:0;
                    padding:0;
                    background:#f8fafc;
                    font-family:Arial,sans-serif;
                    color:#0f172a;
                ">
                    <table width="100%%"
                           cellpadding="0"
                           cellspacing="0">
                        <tr>
                            <td align="center"
                                style="padding:40px 16px;">

                                <table width="100%%"
                                       cellpadding="0"
                                       cellspacing="0"
                                       style="
                                           max-width:560px;
                                           background:#ffffff;
                                           border-radius:16px;
                                           overflow:hidden;
                                           box-shadow:0 10px 30px rgba(15,23,42,.08);
                                       ">

                                    <tr>
                                        <td style="
                                            padding:30px;
                                            text-align:center;
                                            background:#0f172a;
                                            color:#ffffff;
                                        ">
                                            <h1 style="margin:0;">
                                                FitLife
                                            </h1>

                                            <p style="margin:8px 0 0;">
                                                Xác minh tài khoản
                                            </p>
                                        </td>
                                    </tr>

                                    <tr>
                                        <td style="padding:32px;">
                                            <p>
                                                Xin chào
                                                <strong>%s</strong>,
                                            </p>

                                            <p style="
                                                color:#475569;
                                                line-height:1.7;
                                            ">
                                                Cảm ơn bạn đã đăng ký
                                                tài khoản FitLife.
                                                Vui lòng nhấn nút dưới đây
                                                để xác minh địa chỉ email.
                                            </p>

                                            <div style="
                                                text-align:center;
                                                margin:28px 0;
                                            ">
                                                <a href="%s"
                                                   style="
                                                       display:inline-block;
                                                       padding:14px 28px;
                                                       background:#0284c7;
                                                       color:#ffffff;
                                                       text-decoration:none;
                                                       border-radius:10px;
                                                       font-weight:bold;
                                                   ">
                                                    Xác minh tài khoản
                                                </a>
                                            </div>

                                            <p style="
                                                color:#475569;
                                                line-height:1.7;
                                            ">
                                                Liên kết có hiệu lực
                                                trong %d giờ.
                                            </p>

                                            <p style="
                                                color:#64748b;
                                                font-size:13px;
                                            ">
                                                Nếu nút không hoạt động,
                                                hãy sao chép liên kết sau:
                                            </p>

                                            <p style="
                                                background:#f1f5f9;
                                                padding:12px;
                                                border-radius:8px;
                                                word-break:break-all;
                                                font-size:12px;
                                            ">
                                                %s
                                            </p>
                                        </td>
                                    </tr>

                                    <tr>
                                        <td style="
                                            padding:20px 32px;
                                            text-align:center;
                                            background:#f8fafc;
                                            color:#64748b;
                                            font-size:12px;
                                        ">
                                            Nếu bạn không đăng ký tài khoản,
                                            vui lòng bỏ qua email này.
                                            <br>
                                            FitLife Team
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(
                displayName,
                verificationLink,
                expirationHours,
                verificationLink
        );
    }
}