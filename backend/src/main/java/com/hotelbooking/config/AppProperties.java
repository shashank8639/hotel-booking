package com.hotelbooking.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Binds {@code app.name} and {@code app.version} from YAML / environment.
 * <p>
 * Coexists with nested prefixes like {@code app.jwt.*} (separate {@link JwtProperties}).
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    /**
     * Human-readable product name (e.g. "Hotel Booking System").
     */
    private String name = "Hotel Booking System";

    /**
     * Deployed artifact / release version.
     */
    private String version = "0.0.1-SNAPSHOT";
}
