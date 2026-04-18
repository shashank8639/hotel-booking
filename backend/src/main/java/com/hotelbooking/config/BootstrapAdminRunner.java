package com.hotelbooking.config;

import com.hotelbooking.entity.Role;
import com.hotelbooking.entity.User;
import com.hotelbooking.repository.RoleRepository;
import com.hotelbooking.repository.UserRepository;
import com.hotelbooking.security.TokenUtils;
import com.hotelbooking.security.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Optional first-admin bootstrap for production / fresh deploys.
 * <p>
 * Set both env vars once, start the app, then remove the password from the environment:
 * <ul>
 *   <li>{@code APP_BOOTSTRAP_ADMIN_EMAIL}</li>
 *   <li>{@code APP_BOOTSTRAP_ADMIN_PASSWORD} (min 8 chars)</li>
 * </ul>
 * No-op if the email already exists or vars are blank.
 */
@Slf4j
@Component
@Order(20)
@RequiredArgsConstructor
public class BootstrapAdminRunner implements ApplicationRunner {

    private final Environment environment;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        String email = trim(environment.getProperty("APP_BOOTSTRAP_ADMIN_EMAIL"));
        String password = environment.getProperty("APP_BOOTSTRAP_ADMIN_PASSWORD");
        if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            return;
        }
        if (password.length() < 8) {
            throw new IllegalStateException("APP_BOOTSTRAP_ADMIN_PASSWORD must be at least 8 characters");
        }

        String normalized = email.toLowerCase();
        if (userRepository.existsByEmail(normalized)) {
            log.info("Bootstrap admin skipped — user already exists: {}", normalized);
            return;
        }

        Role adminRole = roleRepository.findByName(UserRole.ADMIN)
                .orElseThrow(() -> new IllegalStateException("ADMIN role is not seeded — apply V4 migration"));
        Role customerRole = roleRepository.findByName(UserRole.CUSTOMER).orElse(null);

        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        if (customerRole != null) {
            roles.add(customerRole);
        }

        User admin = User.builder()
                .email(normalized)
                .password(TokenUtils.encode(passwordEncoder, password))
                .firstName(trim(environment.getProperty("APP_BOOTSTRAP_ADMIN_FIRST_NAME", "Platform")))
                .lastName(trim(environment.getProperty("APP_BOOTSTRAP_ADMIN_LAST_NAME", "Admin")))
                .roles(roles)
                .build();
        userRepository.save(admin);
        log.warn("Bootstrap ADMIN created for {}. Remove APP_BOOTSTRAP_ADMIN_PASSWORD from the environment.", normalized);
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }
}
