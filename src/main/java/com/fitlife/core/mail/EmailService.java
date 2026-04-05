package com.fitlife.core.mail;

import org.springframework.scheduling.annotation.Async;

public interface EmailService {
    @Async
    void sendWelcomeEmail(String toEmail, String fullName);

    @Async
    void sendPasswordResetEmail(String toEmail, String otp);
}
