package com.hotelbooking.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Mail settings bound from environment variables (application.yml stays unchanged).
 * <p>
 * Examples:
 * <ul>
 *   <li>{@code APP_MAIL_ENABLED=true}</li>
 *   <li>{@code APP_MAIL_HOST=smtp.gmail.com}</li>
 *   <li>{@code APP_MAIL_PORT=587}</li>
 *   <li>{@code APP_MAIL_USERNAME=...}</li>
 *   <li>{@code APP_MAIL_PASSWORD=...}</li>
 *   <li>{@code APP_MAIL_FROM=noreply@grandhorizon.example}</li>
 * </ul>
 * <p>
 * While {@code pom.xml} cannot add {@code spring-boot-starter-mail}, emails are delivered
 * via {@link com.hotelbooking.notification.LoggingEmailSender} / outbox writer.
 * The property shape mirrors Spring Mail so swapping to {@code JavaMailSender} later is trivial.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.mail")
public class MailProperties {

    /**
     * When false, emails are logged only (safe default for local/dev).
     */
    private boolean enabled = false;

    private String host = "localhost";
    private int port = 587;
    private String username = "";
    private String password = "";
    private String from = "noreply@grandhorizon.example";
    private String fromName = "Grand Horizon Hotel";
    private String supportEmail = "support@grandhorizon.example";
    private String hotelName = "Grand Horizon Hotel";

    /** Front-desk / ops mailbox CC'd on booking cancellations. */
    private String opsEmail = "ops@grandhorizon.example";

    /** Default template locale when guest has none. */
    private String defaultLocale = "en";

    /**
     * Transport: {@code logging} (default .eml/log) or {@code smtp} (JavaMailSender when on classpath).
     */
    private String transport = "logging";

    /**
     * Template engine: {@code placeholder} (default) or {@code thymeleaf} when Thymeleaf is on classpath.
     */
    private String templateEngine = "placeholder";

    /** Max password-reset emails per recipient in the rolling window. */
    private int passwordResetMaxPerHour = 3;

    /** Rolling window for password-reset rate limit (minutes). */
    private int passwordResetWindowMinutes = 60;

    /**
     * When enabled=true, also write .eml files here for inspection (no SMTP library required).
     */
    private String outboxDirectory = "target/email-outbox";

    private boolean smtpAuth = true;
    private boolean startTls = true;
}
