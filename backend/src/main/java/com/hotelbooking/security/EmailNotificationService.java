package com.hotelbooking.security;

/**
 * Placeholder for password reset and notification emails.
 * Implementation will be provided in a later module (e.g. JavaMail / SendGrid).
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
