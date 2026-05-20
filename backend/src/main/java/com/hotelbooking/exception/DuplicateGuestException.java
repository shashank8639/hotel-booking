package com.hotelbooking.exception;

import org.springframework.http.HttpStatus;

public class DuplicateGuestException extends ApiException {

    public DuplicateGuestException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
