package com.hotelbooking.exception;

import org.springframework.http.HttpStatus;

public class GuestNotFoundException extends ApiException {

    public GuestNotFoundException(Long id) {
        super("Guest not found with id: " + id, HttpStatus.NOT_FOUND);
    }

    public GuestNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
