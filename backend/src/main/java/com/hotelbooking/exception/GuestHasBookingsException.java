package com.hotelbooking.exception;

public class GuestHasBookingsException extends RuntimeException {

    public GuestHasBookingsException(Long guestId) {
        super("Cannot delete guest with id " + guestId + " because active bookings exist");
    }
}
