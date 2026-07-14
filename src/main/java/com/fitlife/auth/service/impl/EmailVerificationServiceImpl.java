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

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final AuthProperties authProperties;

    @Override
    public void createAndSendVerificationToken(User user) {
        tokenRepository.deleteAllByUserIdAndUsedFalse(
                user.getId()
        );

        String rawToken =
                AuthTokenUtils.generateSecureToken();

        String tokenHash =
                AuthTokenUtils.hashToken(rawToken);

        EmailVerificationToken token =
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

        tokenRepository.save(token);

        String verificationLink =
                authProperties.getFrontendVerificationUrl()
                        + "?token="
                        + rawToken;

        String displayName =
                user.getFullName() == null
                        || user.getFullName().isBlank()
                        ? "bạn"
                        : user.getFullName();

        String htmlContent =
                buildVerificationEmail(
                        displayName,
                        verificationLink
                );

        emailService.sendHtmlMail(
                user.getEmail(),
                "Xác minh tài khoản FitLife",
                htmlContent
        );
    }

    @Override
    public void verifyEmail(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new AppException(
                    ErrorCode.INVALID_EMAIL_VERIFICATION_TOKEN
            );
        }

        String tokenHash =
                AuthTokenUtils.hashToken(rawToken);

        EmailVerificationToken verificationToken =
                tokenRepository
                        .findByTokenHash(tokenHash)
                        .orElseThrow(() ->
                                new AppException(
                                        ErrorCode.INVALID_EMAIL_VERIFICATION_TOKEN
                                )
                        );

        User user = verificationToken.getUser();

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
        User user = userRepository
                .findByEmail(email)
                .orElse(null);

        if (user == null) {
            return;
        }

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            return;
        }

        createAndSendVerificationToken(user);
    }

    private String buildVerificationEmail(
            String displayName,
            String verificationLink
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
                                                để xác minh email.
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
                                                trong 24 giờ.
                                            </p>

                                            <p style="
                                                color:#64748b;
                                                font-size:13px;
                                            ">
                                                Nếu nút không hoạt động,
                                                sao chép liên kết sau:
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
                verificationLink
        );
    }
}