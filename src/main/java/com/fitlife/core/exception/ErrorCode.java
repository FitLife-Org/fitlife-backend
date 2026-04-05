package com.fitlife.core.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    USER_NOT_FOUND(404, "Không tìm thấy thông tin người dùng!"),
    MEMBER_NOT_FOUND(404, "Không tìm thấy thông tin hội viên!"),
    PHONE_ALREADY_EXISTS(400, "Số điện thoại này đã được đăng ký!"),
    INVALID_CREDENTIALS(401, "Tài khoản hoặc mật khẩu không chính xác!"),
    UNCATEGORIZED_EXCEPTION(500, "Lỗi hệ thống không xác định, vui lòng thử lại sau!");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}