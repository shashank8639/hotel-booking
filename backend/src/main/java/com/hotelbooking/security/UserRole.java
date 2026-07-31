package com.hotelbooking.security;

/**
 * Application roles for RBAC.
 * Spring Security expects the {@code ROLE_} prefix at runtime.
 */
public enum UserRole {
    ADMIN,
    CUSTOMER
}
