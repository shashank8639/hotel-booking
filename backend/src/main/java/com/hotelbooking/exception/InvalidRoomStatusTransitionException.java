package com.hotelbooking.exception;

import org.springframework.http.HttpStatus;

public class InvalidRoomStatusTransitionException extends ApiException {

    public InvalidRoomStatusTransitionException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
