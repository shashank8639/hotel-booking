package com.hotelbooking.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Booking-engine tunables (soft-hold / pending expiry).
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.booking")
public class BookingProperties {

    /**
     * Minutes a newly created PENDING booking holds inventory before auto-cancel.
     */
    private int pendingHoldMinutes = 15;

    /**
     * When false, the scheduled expiry job is a no-op (useful in some tests).
     */
    private boolean expiryEnabled = true;

    /**
     * Fixed delay between expiry scans (ms). Default 5 minutes.
     */
    private long expiryFixedDelayMs = 300_000L;
}
