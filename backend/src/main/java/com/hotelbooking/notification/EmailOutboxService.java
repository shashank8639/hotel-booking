package com.hotelbooking.notification;

import com.hotelbooking.config.MailProperties;
import com.hotelbooking.database.EmailOutboxStatus;
import com.hotelbooking.database.EmailType;
import com.hotelbooking.entity.EmailOutbox;
import com.hotelbooking.repository.EmailOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * Persists outbound emails for audit / retry (transactional outbox pattern).
 */
@Service
@RequiredArgsConstructor
public class EmailOutboxService {

    private final EmailOutboxRepository emailOutboxRepository;

    @Transactional
    public EmailOutbox enqueue(EmailMessage message, EmailType type, String templateName, String locale) {
        EmailOutbox row = EmailOutbox.builder()
                .toAddress(message.getTo())
                .ccAddress(message.getCc())
                .subject(message.getSubject())
                .templateName(templateName)
                .emailType(type)
                .locale(StringUtils.hasText(locale) ? locale : "en")
                .bodyHtml(message.getHtmlBody())
                .status(EmailOutboxStatus.PENDING)
                .attemptCount(0)
                .build();
        return emailOutboxRepository.save(row);
    }

    @Transactional
    public void markSent(Long outboxId) {
        emailOutboxRepository.findById(outboxId).ifPresent(row -> {
            row.setStatus(EmailOutboxStatus.SENT);
            row.setAttemptCount(row.getAttemptCount() + 1);
            row.setSentAt(LocalDateTime.now());
            row.setLastError(null);
            emailOutboxRepository.save(row);
        });
    }

    @Transactional
    public void markFailed(Long outboxId, String error) {
        emailOutboxRepository.findById(outboxId).ifPresent(row -> {
            int attempts = row.getAttemptCount() + 1;
            row.setAttemptCount(attempts);
            row.setLastError(truncate(error));
            row.setStatus(attempts >= 5 ? EmailOutboxStatus.DEAD : EmailOutboxStatus.FAILED);
            emailOutboxRepository.save(row);
        });
    }

    @Transactional
    public void markSuppressed(Long outboxId, String reason) {
        emailOutboxRepository.findById(outboxId).ifPresent(row -> {
            row.setStatus(EmailOutboxStatus.SUPPRESSED);
            row.setLastError(truncate(reason));
            emailOutboxRepository.save(row);
        });
    }

    private static String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= 1000 ? value : value.substring(0, 1000);
    }
}
