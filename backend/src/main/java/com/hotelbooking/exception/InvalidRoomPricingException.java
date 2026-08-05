package com.hotelbooking.exception;

import org.springframework.http.HttpStatus;

public class InvalidRoomPricingException extends ApiException {

    public InvalidRoomPricingException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
