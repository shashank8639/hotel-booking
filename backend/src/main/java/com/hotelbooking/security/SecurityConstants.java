package com.hotelbooking.security;

/**
 * Constants used across the security layer.
 */
public final class SecurityConstants {

    public static final String ROLE_PREFIX = "ROLE_";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String TOKEN_TYPE_CLAIM = "type";
    public static final String ACCESS_TOKEN_TYPE = "ACCESS";
    public static final String REFRESH_TOKEN_TYPE = "REFRESH";
    public static final String ROLES_CLAIM = "roles";

    public static final String AUTH_BASE_PATH = "/auth";
    public static final String AUTH_REGISTER = "/auth/register";
    public static final String AUTH_LOGIN = "/auth/login";
    public static final String AUTH_REFRESH = "/auth/refresh";
    public static final String AUTH_FORGOT_PASSWORD = "/auth/forgot-password";
    public static final String AUTH_RESET_PASSWORD = "/auth/reset-password";

    public static final long PASSWORD_RESET_EXPIRY_MINUTES = 30L;
    public static final long DEFAULT_REFRESH_EXPIRATION_MS = 604_800_000L; // 7 days

    private SecurityConstants() {
    }
}
