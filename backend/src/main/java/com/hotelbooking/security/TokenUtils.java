package com.hotelbooking.security;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility helpers for token and password-reset operations.
 */
public final class TokenUtils {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private TokenUtils() {
    }

    /**
     * Generates a cryptographically secure random token for refresh/reset flows.
     */
    public static String generateSecureToken(int byteLength) {
        byte[] bytes = new byte[byteLength];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Verifies a raw password against a BCrypt hash.
     */
    public static boolean matches(PasswordEncoder encoder, String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }

    /**
     * Hashes a raw password with BCrypt.
     */
    public static String encode(PasswordEncoder encoder, String rawPassword) {
        return encoder.encode(rawPassword);
    }
}
