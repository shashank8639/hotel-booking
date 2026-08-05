package com.hotelbooking.exception;

import org.springframework.http.HttpStatus;

public class DuplicateRoomException extends ApiException {

    public DuplicateRoomException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
