package com.hotelbooking.exception;

import org.springframework.http.HttpStatus;

public class InvalidBookingSortException extends ApiException {

    public InvalidBookingSortException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
