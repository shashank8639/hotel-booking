package com.hotelbooking.exception;

import org.springframework.http.HttpStatus;

public class InvalidBookingStatusTransitionException extends ApiException {

    public InvalidBookingStatusTransitionException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
