package com.fitlife.auth.validation;

public final class PasswordPolicy {

    public static final String REGEX =
            "^(?=.*[A-Za-z])(?=.*\\d)(?!\\s+$).{8,100}$";

    public static final String MESSAGE =
            "Password must be between 8 and 100 characters "
                    + "and contain at least one letter and one number";

    private PasswordPolicy() {
    }
}