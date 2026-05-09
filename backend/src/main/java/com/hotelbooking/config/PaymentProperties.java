package com.hotelbooking.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Payment-module tunables (PENDING order expiry, GST, FX table).
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.payment")
public class PaymentProperties {

    /** Minutes before an unpaid PENDING Razorpay order is auto-cancelled. */
    private int pendingOrderMinutes = 30;

    private boolean expiryEnabled = true;

    private long expiryFixedDelayMs = 300_000L;

    /** GST rate for exclusive tax model (e.g. 0.18 = 18%). */
    private BigDecimal gstRate = new BigDecimal("0.18");

    /** Hotel settlement / books currency. */
    private String baseCurrency = "INR";

    /**
     * FX rates: 1 unit of key currency → value in {@link #baseCurrency}.
     * Example: USD → 83.50 means 1 USD = 83.50 INR.
     */
    private Map<String, BigDecimal> fxRatesToBase = new HashMap<>(Map.of(
            "INR", BigDecimal.ONE,
            "USD", new BigDecimal("83.50"),
            "EUR", new BigDecimal("90.00")
    ));
}
