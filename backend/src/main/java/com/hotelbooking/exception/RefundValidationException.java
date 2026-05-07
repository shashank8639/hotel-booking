package com.hotelbooking.exception;

import org.springframework.http.HttpStatus;

public class RefundValidationException extends ApiException {
    public RefundValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
