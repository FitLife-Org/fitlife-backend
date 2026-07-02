package com.fitlife.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // =========================
    // Common errors: 1000 - 1999
    // =========================
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

    UNAUTHORIZED(
            1003,
            "Unauthorized",
            HttpStatus.UNAUTHORIZED
    ),

    FORBIDDEN(
            1004,
            "You do not have permission to access this resource",
            HttpStatus.FORBIDDEN
    ),


    // =========================
    // User errors: 2000 - 2999
    // =========================
    USER_NOT_FOUND(
            2001,
            "User not found",
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


    // =========================
    // Member errors: 3000 - 3999
    // =========================
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


    // =========================
    // Role errors: 4000 - 4999
    // =========================
    ROLE_NOT_FOUND(
            4001,
            "Role not found",
            HttpStatus.NOT_FOUND
    ),


    // =========================
    // Auth errors: 5000 - 5999
    // =========================
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

    INVALID_TOKEN(
            5003,
            "Invalid token",
            HttpStatus.UNAUTHORIZED
    ),

    EXPIRED_TOKEN(
            5004,
            "Token has expired",
            HttpStatus.UNAUTHORIZED
    ),

    RESET_TOKEN_INVALID(
            5005,
            "Reset token is invalid",
            HttpStatus.BAD_REQUEST
    ),

    RESET_TOKEN_EXPIRED(
            5006,
            "Reset token has expired",
            HttpStatus.BAD_REQUEST
    ),

    PASSWORD_CONFIRM_NOT_MATCH(
            5007,
            "Password confirmation does not match",
            HttpStatus.BAD_REQUEST
    ),

    OTP_INVALID(
            5008,
            "OTP is invalid",
            HttpStatus.BAD_REQUEST
    ),

    OTP_EXPIRED(
            5009,
            "OTP has expired",
            HttpStatus.BAD_REQUEST
    ),

    NEW_PASSWORD_SAME_AS_OLD(
            5010,
            "New password must be different from old password",
            HttpStatus.BAD_REQUEST
    ),

    ACCOUNT_LOCKED(
            5011,
            "Account is locked",
            HttpStatus.FORBIDDEN
    ),

    ACCOUNT_INACTIVE(
            5012,
            "Account is inactive",
            HttpStatus.FORBIDDEN
    ),

    ACCOUNT_DELETED(
            5013,
            "Account has been deleted",
            HttpStatus.FORBIDDEN
    ),

    CURRENT_PASSWORD_INCORRECT(
            5014,
            "Current password is incorrect",
            HttpStatus.BAD_REQUEST
    ),


    // =========================
    // Equipment errors: 6000 - 6999
    // =========================
    EQUIPMENT_NOT_FOUND(
            6001,
            "Equipment not found",
            HttpStatus.NOT_FOUND
    ),

    EQUIPMENT_CODE_ALREADY_EXISTS(
            6002,
            "Equipment code already exists",
            HttpStatus.BAD_REQUEST
    ),


    // =========================
    // Package errors: 7000 - 7999
    // =========================
    PACKAGE_NOT_FOUND(
            7001,
            "Package not found",
            HttpStatus.NOT_FOUND
    ),

    PACKAGE_CODE_ALREADY_EXISTS(
            7002,
            "Package code already exists",
            HttpStatus.BAD_REQUEST
    ),

    PACKAGE_INACTIVE(
            7003,
            "Package is inactive",
            HttpStatus.BAD_REQUEST
    ),

    GYM_PACKAGE_NOT_FOUND(
            7004,
            "Gym package not found",
            HttpStatus.NOT_FOUND
    ),

    GYM_PACKAGE_INACTIVE(
            7005,
            "Gym package is inactive",
            HttpStatus.BAD_REQUEST
    ),


    // =========================
    // Subscription errors: 8000 - 8999
    // =========================
    SUBSCRIPTION_NOT_FOUND(
            8001,
            "Subscription not found",
            HttpStatus.NOT_FOUND
    ),

    SUBSCRIPTION_NOT_OWNED_BY_MEMBER(
            8002,
            "Subscription does not belong to current member",
            HttpStatus.FORBIDDEN
    ),

    ACTIVE_SUBSCRIPTION_EXISTS(
            8003,
            "Member already has an active subscription",
            HttpStatus.BAD_REQUEST
    ),

    CANNOT_CANCEL_ACTIVE_SUBSCRIPTION(
            8004,
            "Cannot cancel active subscription",
            HttpStatus.BAD_REQUEST
    ),

    SUBSCRIPTION_ALREADY_CANCELLED(
            8005,
            "Subscription already cancelled",
            HttpStatus.BAD_REQUEST
    ),

    INVALID_SUBSCRIPTION_STATUS(
            8006,
            "Invalid subscription status",
            HttpStatus.BAD_REQUEST
    ),


    // =========================
    // Invoice errors: 9000 - 9998
    // =========================
    INVOICE_NOT_FOUND(
            9001,
            "Invoice not found",
            HttpStatus.NOT_FOUND
    ),

    INVOICE_ALREADY_EXISTS(
            9002,
            "Invoice already exists for this subscription",
            HttpStatus.BAD_REQUEST
    ),

    INVOICE_ALREADY_PAID(
            9003,
            "Invoice already paid",
            HttpStatus.BAD_REQUEST
    ),

    INVOICE_CANCELLED(
            9004,
            "Invoice has been cancelled",
            HttpStatus.BAD_REQUEST
    ),

    INVOICE_NOT_OWNED_BY_MEMBER(
            9005,
            "Invoice does not belong to current member",
            HttpStatus.FORBIDDEN
    ),

    INVALID_INVOICE_STATUS(
            9006,
            "Invalid invoice status",
            HttpStatus.BAD_REQUEST
    ),


    // =========================
    // Payment errors: 10000 - 10999
    // =========================
    PAYMENT_NOT_FOUND(
            10001,
            "Payment not found",
            HttpStatus.NOT_FOUND
    ),

    PAYMENT_ALREADY_SUCCESS(
            10002,
            "Payment already success",
            HttpStatus.BAD_REQUEST
    ),

    INVALID_PAYMENT_STATUS(
            10003,
            "Invalid payment status",
            HttpStatus.BAD_REQUEST
    ),

    PAYMENT_NOT_OWNED_BY_MEMBER(
            10004,
            "Payment does not belong to current member",
            HttpStatus.FORBIDDEN
    ),

    SUCCESS_PAYMENT_ALREADY_EXISTS(
            10005,
            "Success payment already exists for this invoice",
            HttpStatus.BAD_REQUEST
    ),

    INVALID_PAYMENT_METHOD(
            10006,
            "Invalid payment method",
            HttpStatus.BAD_REQUEST
    ),

    CANNOT_CREATE_PAYMENT_FOR_PAID_INVOICE(
            10007,
            "Cannot create payment for paid invoice",
            HttpStatus.BAD_REQUEST
    ),

    CANNOT_CREATE_PAYMENT_FOR_CANCELLED_INVOICE(
            10008,
            "Cannot create payment for cancelled invoice",
            HttpStatus.BAD_REQUEST
    ),

    BODY_METRIC_NOT_FOUND(
        11001,
                "Body metric not found",
        HttpStatus.NOT_FOUND
        ),

    WEIGHT_INVALID(
        11002,
                "Weight is invalid",
        HttpStatus.BAD_REQUEST
        ),

    HEIGHT_INVALID(11003,
                "Height is invalid",
        HttpStatus.BAD_REQUEST
        ),

    BODY_FAT_INVALID(
        11004,
                "Body fat percent is invalid",
        HttpStatus.BAD_REQUEST
        ),

    MUSCLE_MASS_INVALID(
        11005,
                "Muscle mass is invalid",
        HttpStatus.BAD_REQUEST
        ),
    AI_SUGGESTION_NOT_FOUND(
            12001,
            "AI suggestion not found",
            HttpStatus.NOT_FOUND
    ),

    AI_PROVIDER_ERROR(
            12002,
            "AI provider error",
            HttpStatus.BAD_GATEWAY
    ),

    AI_RESPONSE_INVALID(
            12003,
            "AI response is invalid",
            HttpStatus.BAD_GATEWAY
    ),

    AI_LIMIT_EXCEEDED(
            12004,
            "AI daily limit exceeded",
            HttpStatus.BAD_REQUEST
    ),

    AI_FEEDBACK_ALREADY_EXISTS(
            12005,
            "AI feedback already exists",
            HttpStatus.BAD_REQUEST
    );

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}