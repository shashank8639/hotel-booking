package com.hotelbooking.exception;

import org.springframework.http.HttpStatus;

public class EmailValidationException extends ApiException {

    public EmailValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
