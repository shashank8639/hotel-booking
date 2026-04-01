package com.hotelbooking.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Debug aid registered inside the Spring Security filter chain (after JWT filter).
 * <p>
 * Enable:
 * <pre>
 * logging.level.com.hotelbooking.security.SecurityFilterTraceFilter=DEBUG
 * logging.level.org.springframework.security=DEBUG
 * </pre>
 */
@Slf4j
public class SecurityFilterTraceFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (log.isDebugEnabled()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            log.debug(
                    "After JWT filter: {} {} → authenticated={}, principal={}, authorities={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    auth != null && auth.isAuthenticated(),
                    auth != null ? auth.getName() : null,
                    auth != null ? auth.getAuthorities() : null
            );
        }
        filterChain.doFilter(request, response);
    }
}
