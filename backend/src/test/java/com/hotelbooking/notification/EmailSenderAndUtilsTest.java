package com.hotelbooking.notification;

import com.hotelbooking.config.MailProperties;
import com.hotelbooking.exception.EmailValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailSenderAndUtilsTest {

    @TempDir
    Path tempDir;

    @Test
    void emailUtils_shouldValidateAndFormat() {
        EmailUtils utils = new EmailUtils();
        utils.validateRecipient("guest@example.com");
        assertThatThrownBy(() -> utils.validateRecipient("bad"))
                .isInstanceOf(EmailValidationException.class);
        assertThat(utils.formatMoney(new BigDecimal("10.5"), "INR")).isEqualTo("INR 10.50");
        assertThat(utils.subject("Hello", "REF")).isEqualTo("Hello — REF");
        assertThat(utils.hasAttachment(new byte[]{1})).isTrue();
        assertThat(utils.hasAttachment(null)).isFalse();
    }

    @Test
    void loggingEmailSender_shouldWriteEmlWithCcWhenEnabled() throws Exception {
        MailProperties props = new MailProperties();
        props.setEnabled(true);
        props.setOutboxDirectory(tempDir.toString());
        props.setFrom("noreply@test.example");
        props.setFromName("Test Hotel");

        LoggingEmailSender sender = new LoggingEmailSender(props);
        sender.send(new EmailMessage()
                .setTo("guest@example.com")
                .setCc("ops@test.example")
                .setSubject("Booking Cancelled")
                .setHtmlBody("<html><body>Hi</body></html>")
                .addAttachment(new EmailMessage.EmailAttachment(
                        "INV.pdf", "application/pdf", "%PDF".getBytes())));

        assertThat(Files.list(tempDir).count()).isEqualTo(1);
        String eml = Files.readString(Files.list(tempDir).findFirst().orElseThrow());
        assertThat(eml).contains("To: guest@example.com");
        assertThat(eml).contains("Cc: ops@test.example");
        assertThat(eml).contains("Content-Type: application/pdf");
        assertThat(eml).contains("INV.pdf");
    }

    @Test
    void passwordResetRateLimiter_shouldBlockAfterMax() {
        MailProperties props = new MailProperties();
        props.setPasswordResetMaxPerHour(2);
        props.setPasswordResetWindowMinutes(60);
        PasswordResetEmailRateLimiter limiter = new PasswordResetEmailRateLimiter(props);

        assertThat(limiter.tryAcquire("a@example.com")).isTrue();
        assertThat(limiter.tryAcquire("a@example.com")).isTrue();
        assertThat(limiter.tryAcquire("a@example.com")).isFalse();
        assertThat(limiter.tryAcquire("b@example.com")).isTrue();
    }
}
