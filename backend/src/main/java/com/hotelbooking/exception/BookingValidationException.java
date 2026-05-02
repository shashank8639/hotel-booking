package com.hotelbooking.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown for booking request rule violations (duplicate room IDs, empty rooms, etc.).
 */
public class BookingValidationException extends ApiException {

    public BookingValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
