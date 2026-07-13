package com.fitlife.auth.service;

public interface MailService {

    void sendVerificationEmail(
            String recipientEmail,
            String fullName,
            String verificationLink
    );
}