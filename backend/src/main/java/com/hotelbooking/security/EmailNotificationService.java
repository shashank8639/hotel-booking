package com.hotelbooking.security;

/**
 * Abstraction for auth-related notification emails (password reset, etc.).
 * Implemented by {@link EmailNotificationServiceImpl}, which routes through Module 8 async notifications.
 */
public interface EmailNotificationService {

    /**
     * Sends a password reset link or token to the user's email address.
     *
     * @param toEmail destination email
     * @param resetToken one-time reset token
     */
    void sendPasswordResetEmail(String toEmail, String resetToken);
}
