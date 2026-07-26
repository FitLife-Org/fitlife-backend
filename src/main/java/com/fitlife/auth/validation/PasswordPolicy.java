package com.fitlife.auth.validation;

public final class PasswordPolicy {

    public static final int MIN_LENGTH = 8;

    /*
     * Có ít nhất:
     * - một chữ cái Latin
     * - một chữ số
     * - tổng độ dài từ 8 ký tự
     */
    public static final String REGEX =
            "^(?=.*[A-Za-z])(?=.*\\d).{8,}$";

    public static final String MESSAGE =
            "Password must contain at least 8 characters, "
                    + "including at least one letter and one number";

    private PasswordPolicy() {
    }
}