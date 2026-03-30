package com.hotelbooking.database;

/**
 * Lifecycle of a hotel listing on the multi-hotel platform.
 * Public search returns only {@link #APPROVED} (+ verified flag in service).
 */
public enum HotelStatus {
    DRAFT,
    PENDING_APPROVAL,
    APPROVED,
    REJECTED,
    SUSPENDED
}
