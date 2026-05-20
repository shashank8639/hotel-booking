package com.hotelbooking.database;

/**
 * Operational availability state of a physical room.
<<<<<<< HEAD
 */
public enum RoomStatus {
    AVAILABLE,
=======
 * Distinct from {@link BookingStatus} which tracks a reservation lifecycle.
 */
public enum RoomStatus {
    AVAILABLE,
    RESERVED,
>>>>>>> feature/module-1-foundation-practice
    OCCUPIED,
    MAINTENANCE,
    OUT_OF_SERVICE
}
