package com.hotelbooking.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * Practice bean for {@code @PreAuthorize} method security.
 * <p>
 * URL rules in {@code SecurityConfig} gate the HTTP layer; this gates the method layer.
 */
@Slf4j
@Service
public class AdminOnlyDemoService {

    /**
     * Only callers with {@code ROLE_ADMIN} may enter.
     * Customers receive {@link org.springframework.security.access.AccessDeniedException}.
     */
    @PreAuthorize("hasRole('ADMIN')")
    public String adminOnlyPing() {
        log.debug("adminOnlyPing invoked");
        return "admin-ok";
    }

    /** Any authenticated user (ADMIN or CUSTOMER). */
    @PreAuthorize("isAuthenticated()")
    public String anyAuthenticatedPing() {
        return "auth-ok";
    }
}
