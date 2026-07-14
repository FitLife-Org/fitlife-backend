package com.fitlife.mail.service.impl;

import com.fitlife.mail.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${fitlife.mail.from:no-reply@fitlife.local}")
    private String mailFrom;

    @Override
    public void sendSimpleMail(
            String to,
            String subject,
            String content
    ) {
        try {
            SimpleMailMessage message =
                    new SimpleMailMessage();

            message.setFrom(mailFrom);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);

            javaMailSender.send(message);

            log.info(
                    "Simple email sent successfully. To: {}",
                    to
            );
        } catch (MailException exception) {
            log.error(
                    "Failed to send simple email. To: {}",
                    to,
                    exception
            );

            throw exception;
        }
    }

    @Override
    public void sendHtmlMail(
            String to,
            String subject,
            String htmlContent
    ) {
        try {
            MimeMessage mimeMessage =
                    javaMailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            mimeMessage,
                            false,
                            "UTF-8"
                    );

            helper.setFrom(mailFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            javaMailSender.send(mimeMessage);

            log.info(
                    "HTML email sent successfully. To: {}",
                    to
            );
        } catch (MessagingException | MailException exception) {
            log.error(
                    "Failed to send HTML email. To: {}",
                    to,
                    exception
            );

            throw new IllegalStateException(
                    "Unable to send HTML email",
                    exception
            );
        }
    }
}