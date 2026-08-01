package com.hotelbooking.exception;

public class GuestNotFoundException extends RuntimeException {

    public GuestNotFoundException(Long id) {
        super("Guest not found with id: " + id);
    }

    public GuestNotFoundException(String message) {
        super(message);
    }
}
