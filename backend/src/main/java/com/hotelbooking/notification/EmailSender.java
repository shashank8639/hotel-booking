package com.hotelbooking.notification;

/**
 * Abstraction over SMTP / provider delivery.
 * <p>
 * Production swap (when pom allows {@code spring-boot-starter-mail}):
 * implement this interface with {@code JavaMailSender} + MimeMessageHelper.
 */
public interface EmailSender {

    void send(EmailMessage message);
}
