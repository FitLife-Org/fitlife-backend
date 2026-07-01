package com.fitlife;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHashGeneratorTest {

    @Test
    void generatePasswordHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String rawPassword = "123456";
        String hash = encoder.encode(rawPassword);

        System.out.println("RAW PASSWORD = " + rawPassword);
        System.out.println("BCrypt HASH  = " + hash);

        assertTrue(encoder.matches(rawPassword, hash));
    }
}