package com.hotelbooking.exception;

import org.springframework.http.HttpStatus;

public class GuestHasBookingsException extends ApiException {

    public GuestHasBookingsException(Long guestId) {
        super("Cannot delete guest with id " + guestId + " because active bookings exist", HttpStatus.CONFLICT);
    }
}
