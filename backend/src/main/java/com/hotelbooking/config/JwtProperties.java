package com.hotelbooking.config;

import com.hotelbooking.security.SecurityConstants;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Binds JWT settings from application.yml under the {@code app.jwt} prefix.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String secret;
    private long expirationMs;
    private long refreshExpirationMs = SecurityConstants.DEFAULT_REFRESH_EXPIRATION_MS;
}
