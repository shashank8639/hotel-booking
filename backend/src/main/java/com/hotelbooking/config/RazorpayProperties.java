package com.hotelbooking.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Razorpay settings bound from environment variables (no application.yml change required).
 * <p>
 * Examples:
 * <ul>
 *   <li>{@code APP_RAZORPAY_KEY_ID}</li>
 *   <li>{@code APP_RAZORPAY_KEY_SECRET}</li>
 *   <li>{@code APP_RAZORPAY_WEBHOOK_SECRET}</li>
 *   <li>{@code APP_RAZORPAY_MOCK_ENABLED=true} for local/demo without live keys</li>
 * </ul>
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.razorpay")
public class RazorpayProperties {

    /**
     * Razorpay Key ID (public). Safe to expose to frontend checkout.
     */
    private String keyId = "";

    /**
     * Razorpay Key Secret (private). Never expose to frontend.
     */
    private String keySecret = "";

    /**
     * Webhook signing secret from Razorpay dashboard.
     */
    private String webhookSecret = "";

    /**
     * When true (default if keys blank), uses in-memory mock gateway — no external HTTP.
     */
    private boolean mockEnabled = true;

    private String currency = "INR";

    private String apiBaseUrl = "https://api.razorpay.com/v1";

    public boolean hasLiveCredentials() {
        return keyId != null && !keyId.isBlank()
                && keySecret != null && !keySecret.isBlank()
                && !mockEnabled;
    }
}
