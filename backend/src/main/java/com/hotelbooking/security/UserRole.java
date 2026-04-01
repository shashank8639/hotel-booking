package com.hotelbooking.security;

/**
 * Application roles for RBAC.
 * Spring Security expects the {@code ROLE_} prefix at runtime.
 */
public enum UserRole {
    ADMIN,
    CUSTOMER,
    /** Property manager on the multi-hotel platform (Module 16). */
    HOTEL_OWNER
}
