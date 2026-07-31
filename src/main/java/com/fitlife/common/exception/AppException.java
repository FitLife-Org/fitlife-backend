package com.fitlife.common.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {

    private final ErrorCode errorCode;

    public AppException(ErrorCode errorCode) {
        this(errorCode, errorCode.getMessage(), null);
    }

    public AppException(
            ErrorCode errorCode,
            String customMessage
    ) {
        this(errorCode, customMessage, null);
    }

    public AppException(
            ErrorCode errorCode,
            Throwable cause
    ) {
        this(errorCode, errorCode.getMessage(), cause);
    }

    public AppException(
            ErrorCode errorCode,
            String customMessage,
            Throwable cause
    ) {
        super(resolveMessage(errorCode, customMessage), cause);

        if (errorCode == null) {
            throw new IllegalArgumentException(
                    "ErrorCode must not be null"
            );
        }

        this.errorCode = errorCode;
    }

    private static String resolveMessage(
            ErrorCode errorCode,
            String customMessage
    ) {
        if (customMessage != null
                && !customMessage.isBlank()) {
            return customMessage;
        }

        return errorCode == null
                ? "Application error"
                : errorCode.getMessage();
    }
}
