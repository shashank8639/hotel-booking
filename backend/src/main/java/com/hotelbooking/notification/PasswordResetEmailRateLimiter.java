package com.hotelbooking.notification;

import com.hotelbooking.config.MailProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory rate limit for password-reset emails (abuse / inbox flooding protection).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordResetEmailRateLimiter {

    private final MailProperties mailProperties;
    private final Map<String, List<Instant>> attemptsByEmail = new ConcurrentHashMap<>();

    /**
     * @return true if send is allowed; false if the recipient is rate-limited
     */
    public boolean tryAcquire(String email) {
        String key = email == null ? "" : email.trim().toLowerCase();
        Instant now = Instant.now();
        int windowMinutes = Math.max(1, mailProperties.getPasswordResetWindowMinutes());
        int max = Math.max(1, mailProperties.getPasswordResetMaxPerHour());
        Instant cutoff = now.minus(windowMinutes, ChronoUnit.MINUTES);

        List<Instant> attempts = attemptsByEmail.computeIfAbsent(key, k -> new ArrayList<>());
        synchronized (attempts) {
            Iterator<Instant> it = attempts.iterator();
            while (it.hasNext()) {
                if (it.next().isBefore(cutoff)) {
                    it.remove();
                }
            }
            if (attempts.size() >= max) {
                log.warn("PASSWORD_RESET rate-limited email={}, windowMinutes={}, max={}",
                        key, windowMinutes, max);
                return false;
            }
            attempts.add(now);
            return true;
        }
    }
}
