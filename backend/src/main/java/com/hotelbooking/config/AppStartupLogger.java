package com.hotelbooking.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Logs bound {@link AppProperties} once the context is ready (practice for ConfigurationProperties).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppStartupLogger implements ApplicationRunner {

    private final AppProperties appProperties;
    private final Environment environment;

    @Override
    public void run(ApplicationArguments args) {
        String profiles = String.join(",", environment.getActiveProfiles());
        if (profiles.isBlank()) {
            profiles = "(default)";
        }
        log.info("Application started: name='{}', version='{}', profiles={}",
                appProperties.getName(),
                appProperties.getVersion(),
                profiles);
    }
}
