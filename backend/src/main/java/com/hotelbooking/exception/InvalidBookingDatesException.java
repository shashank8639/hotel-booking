package com.hotelbooking.exception;

import org.springframework.http.HttpStatus;

public class InvalidBookingDatesException extends ApiException {

    public InvalidBookingDatesException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
