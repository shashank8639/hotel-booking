package com.hotelbooking.database;

/**
 * Operational availability state of a physical room.
 * Distinct from {@link BookingStatus} which tracks a reservation lifecycle.
 */
public enum RoomStatus {
    AVAILABLE,
    RESERVED,
    OCCUPIED,
    /** Housekeeping turnaround after checkout — not sellable until AVAILABLE. */
    CLEANING,
    MAINTENANCE,
    OUT_OF_SERVICE
}
