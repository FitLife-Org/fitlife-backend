package com.fitlife.auth.service;

import com.fitlife.user.entity.User;

public interface EmailVerificationService {

    /**
     * Hủy các token xác minh chưa dùng trước đó,
     * tạo token mới và gửi email xác minh.
     */
    void createAndSendVerificationToken(User user);

    /**
     * Xác minh email bằng raw token nhận từ frontend.
     */
    void verifyEmail(String rawToken);

    /**
     * Gửi lại email xác minh.
     *
     * Method không phát sinh lỗi khi email không tồn tại
     * để tránh tiết lộ tài khoản trong hệ thống.
     */
    void resendVerificationEmail(String email);
}