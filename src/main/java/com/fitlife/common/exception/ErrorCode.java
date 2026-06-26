package com.fitlife.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // Common errors
    UNCATEGORIZED_EXCEPTION(
            9999,
            "Uncategorized error",
            HttpStatus.INTERNAL_SERVER_ERROR
    ),

    INVALID_REQUEST(
            1000,
            "Invalid request",
            HttpStatus.BAD_REQUEST
    ),

    VALIDATION_FAILED(
            1001,
            "Validation failed",
            HttpStatus.BAD_REQUEST
    ),

    METHOD_NOT_SUPPORTED(
            1002,
            "Request method is not supported",
            HttpStatus.METHOD_NOT_ALLOWED
    ),

    // User errors
    USER_NOT_FOUND(
            2001,
            "User not found",
            // "If the email exists, a password reset email has been sent", // Avoid strangers snooping on emails
            HttpStatus.NOT_FOUND
    ),

    EMAIL_ALREADY_EXISTS(
            2002,
            "Email already exists",
            HttpStatus.BAD_REQUEST
    ),

    PHONE_ALREADY_EXISTS(
            2003,
            "Phone number already exists",
            HttpStatus.BAD_REQUEST
    ),

    USERNAME_ALREADY_EXISTS(
            2004,
            "Username already exists",
            HttpStatus.BAD_REQUEST
    ),

    // Member errors
    MEMBER_NOT_FOUND(
            3001,
            "Member not found",
            HttpStatus.NOT_FOUND
    ),

    MEMBER_NO_ACCOUNT(
            3002,
            "Member does not have a login account",
            HttpStatus.BAD_REQUEST
    ),

    // Role errors
    ROLE_NOT_FOUND(
            4001,
            "Role not found",
            HttpStatus.NOT_FOUND
    ),

    // Auth errors
    INVALID_CREDENTIALS(
            5001,
            "Invalid email or password",
            HttpStatus.UNAUTHORIZED
    ),

    UNAUTHENTICATED(
            5002,
            "Unauthenticated",
            HttpStatus.UNAUTHORIZED
    ),

    UNAUTHORIZED(
            5003,
            "You do not have permission to access this resource",
            HttpStatus.FORBIDDEN
    ),

    INVALID_TOKEN(
            5004,
            "Invalid token",
            HttpStatus.UNAUTHORIZED
    ),

    EXPIRED_TOKEN(
            5005,
            "Token has expired",
            HttpStatus.UNAUTHORIZED
    ),

    RESET_TOKEN_INVALID(
            5006,
            "Reset token is invalid",
            HttpStatus.BAD_REQUEST
    ),

    RESET_TOKEN_EXPIRED(
            5007,
            "Reset token has expired",
            HttpStatus.BAD_REQUEST
    ),

    PASSWORD_CONFIRM_NOT_MATCH(5008, "Password confirmation does not match", HttpStatus.BAD_REQUEST),
    OTP_INVALID(5009, "OTP is invalid", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED(5010, "OTP has expired", HttpStatus.BAD_REQUEST),
    NEW_PASSWORD_SAME_AS_OLD(5011, "New password must be different from old password", HttpStatus.BAD_REQUEST),;

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}