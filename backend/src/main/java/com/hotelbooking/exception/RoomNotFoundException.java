package com.hotelbooking.exception;

import org.springframework.http.HttpStatus;

public class RoomNotFoundException extends ApiException {

    public RoomNotFoundException(Long id) {
        super("Room not found with id: " + id, HttpStatus.NOT_FOUND);
    }

    public RoomNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
