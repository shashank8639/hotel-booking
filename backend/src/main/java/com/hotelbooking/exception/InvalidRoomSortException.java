package com.hotelbooking.exception;

import org.springframework.http.HttpStatus;

public class InvalidRoomSortException extends ApiException {

    public InvalidRoomSortException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
