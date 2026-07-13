package com.fitlife.auth.service.impl;

import com.fitlife.auth.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Value("${fitlife.mail.from}")
    private String senderEmail;

    @Override
    public void sendVerificationEmail(
            String recipientEmail,
            String fullName,
            String verificationLink
    ) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(senderEmail);
        message.setTo(recipientEmail);
        message.setSubject("Xác minh tài khoản FitLife");

        message.setText("""
                Xin chào %s,

                Cảm ơn bạn đã đăng ký tài khoản FitLife.

                Vui lòng truy cập liên kết dưới đây để xác minh email:

                %s

                Liên kết này có hiệu lực trong 24 giờ.

                Nếu bạn không đăng ký tài khoản FitLife,
                vui lòng bỏ qua email này.

                FitLife Team
                """.formatted(
                fullName == null || fullName.isBlank()
                        ? "bạn"
                        : fullName,
                verificationLink
        ));

        mailSender.send(message);
    }
}