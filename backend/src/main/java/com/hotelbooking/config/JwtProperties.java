package com.hotelbooking.config;

<<<<<<< HEAD
=======
import com.hotelbooking.security.SecurityConstants;
>>>>>>> feature/module-1-foundation-practice
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Binds JWT settings from application.yml under the {@code app.jwt} prefix.
<<<<<<< HEAD
 * Implementation of token generation/validation comes in a later module.
=======
>>>>>>> feature/module-1-foundation-practice
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String secret;
    private long expirationMs;
<<<<<<< HEAD
=======
    private long refreshExpirationMs = SecurityConstants.DEFAULT_REFRESH_EXPIRATION_MS;
>>>>>>> feature/module-1-foundation-practice
}
