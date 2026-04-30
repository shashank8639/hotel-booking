package com.hotelbooking.notification;

import com.hotelbooking.exception.EmailValidationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/**
 * Shared helpers for recipient checks, subjects, and display formatting.
 */
@Component
public class EmailUtils {

    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    public void validateRecipient(String email) {
        if (!StringUtils.hasText(email)) {
            throw new EmailValidationException("Guest email is required for notification");
        }
        if (!EMAIL.matcher(email.trim()).matches()) {
            throw new EmailValidationException("Invalid email format: " + email);
        }
    }

    public String subject(String prefix, String reference) {
        return prefix + " — " + reference;
    }

    public String formatDate(LocalDate date) {
        return date == null ? "-" : DATE.format(date);
    }

    public String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "-" : DATE_TIME.format(dateTime);
    }

    public String formatMoney(BigDecimal amount, String currency) {
        if (amount == null) {
            return "-";
        }
        String cur = StringUtils.hasText(currency) ? currency : "INR";
        return cur + " " + amount.setScale(2, RoundingMode.HALF_UP);
    }

    public boolean hasAttachment(byte[] bytes) {
        return bytes != null && bytes.length > 0;
    }
}
