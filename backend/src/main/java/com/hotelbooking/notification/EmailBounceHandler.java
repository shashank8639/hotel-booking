package com.hotelbooking.notification;

import com.hotelbooking.database.EmailSuppressionReason;
import com.hotelbooking.entity.EmailSuppression;
import com.hotelbooking.entity.Guest;
import com.hotelbooking.repository.EmailSuppressionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Bounce / complaint handling design (Module 8 Part F).
 * <p>
 * Production flow:
 * <ol>
 *   <li>ESP (SES/SendGrid) POSTs bounce/complaint webhook</li>
 *   <li>Verify provider signature</li>
 *   <li>{@link #recordBounce(String, EmailSuppressionReason, String)} persists suppression</li>
 *   <li>Future sends to that address are blocked via {@link #isSuppressed(String)}</li>
 * </ol>
 * Soft bounces may be temporary; hard bounces and complaints stay suppressed until cleared.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailBounceHandler {

    private final EmailSuppressionRepository emailSuppressionRepository;

    public boolean isSuppressed(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        return emailSuppressionRepository.existsByEmailIgnoreCaseAndActiveTrue(email.trim());
    }

    @Transactional
    public void recordBounce(String email, EmailSuppressionReason reason, String detail) {
        if (!StringUtils.hasText(email)) {
            return;
        }
        String normalized = email.trim().toLowerCase();
        EmailSuppression existing = emailSuppressionRepository.findByEmailIgnoreCaseAndActiveTrue(normalized)
                .orElse(null);
        if (existing != null) {
            existing.setReason(reason);
            existing.setDetail(detail);
            emailSuppressionRepository.save(existing);
        } else {
            emailSuppressionRepository.save(EmailSuppression.builder()
                    .email(normalized)
                    .reason(reason)
                    .detail(detail)
                    .active(true)
                    .build());
        }
        log.info("EMAIL SUPPRESSION recorded email={}, reason={}", normalized, reason);
    }

    /**
     * Guest preference gate: transactional mail respects transactional flag;
     * marketing mail requires marketing opt-in.
     */
    public boolean allowsTransactional(Guest guest) {
        return guest == null || guest.isTransactionalEmailsEnabled();
    }

    public boolean allowsMarketing(Guest guest) {
        return guest != null && guest.isMarketingEmailsEnabled();
    }
}
