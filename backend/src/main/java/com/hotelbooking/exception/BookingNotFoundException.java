package com.hotelbooking.exception;

import org.springframework.http.HttpStatus;

public class BookingNotFoundException extends ApiException {

    public BookingNotFoundException(Long id) {
        super("Booking not found with id: " + id, HttpStatus.NOT_FOUND);
    }
}
