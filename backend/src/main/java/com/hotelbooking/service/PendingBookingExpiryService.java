package com.hotelbooking.service;

import com.hotelbooking.config.BookingProperties;
import com.hotelbooking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Releases inventory held by unpaid / unconfirmed PENDING bookings after soft-hold expiry.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PendingBookingExpiryService {

    private final BookingRepository bookingRepository;
    private final BookingProperties bookingProperties;

    @Scheduled(fixedDelayString = "${app.booking.expiry-fixed-delay-ms:300000}")
    @Transactional
    public void expirePendingHolds() {
        if (!bookingProperties.isExpiryEnabled()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        int cancelled = bookingRepository.cancelExpiredPendingHolds(now);
        if (cancelled > 0) {
            log.info("Soft-hold expiry: auto-cancelled {} PENDING booking(s) with hold_expires_at < {}", cancelled, now);
        } else {
            log.debug("Soft-hold expiry sweep: no expired PENDING holds at {}", now);
        }
    }
}
