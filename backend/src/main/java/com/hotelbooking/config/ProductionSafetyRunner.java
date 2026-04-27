package com.hotelbooking.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Fails fast on {@code prod} if unsafe defaults are still present.
 */
@Slf4j
@Component
@Order(10)
@RequiredArgsConstructor
public class ProductionSafetyRunner implements ApplicationRunner {

    private final Environment environment;
    private final RazorpayProperties razorpayProperties;
    private final JwtProperties jwtProperties;

    @Override
    public void run(ApplicationArguments args) {
        if (!environment.acceptsProfiles(Profiles.of("prod"))) {
            return;
        }

        String jwtSecret = jwtProperties.getSecret();
        if (!StringUtils.hasText(jwtSecret)
                || jwtSecret.contains("change-me")
                || jwtSecret.length() < 32) {
            throw new IllegalStateException(
                    "prod profile requires a strong JWT_SECRET (32+ chars, not the example value)"
            );
        }

        if (razorpayProperties.isMockEnabled()) {
            throw new IllegalStateException(
                    "prod profile forbids app.razorpay.mock-enabled=true — set APP_RAZORPAY_MOCK_ENABLED=false and use live keys"
            );
        }

        if (!razorpayProperties.hasLiveCredentials()) {
            throw new IllegalStateException(
                    "prod profile requires APP_RAZORPAY_KEY_ID and APP_RAZORPAY_KEY_SECRET with mock disabled"
            );
        }

        log.info("Production safety checks passed (JWT secret length, Razorpay live mode)");
    }
}
