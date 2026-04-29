package com.hotelbooking.exception;

import org.springframework.http.HttpStatus;

public class HotelNotFoundException extends ApiException {
    public HotelNotFoundException(Long id) {
        super("Hotel not found with id: " + id, HttpStatus.NOT_FOUND);
    }

    public HotelNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
