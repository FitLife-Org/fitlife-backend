package com.fitlife.auth.service;

import com.fitlife.user.entity.User;

public interface EmailVerificationService {

    void createAndSendVerificationToken(User user);

    void verifyEmail(String rawToken);

    void resendVerificationEmail(String email);
}