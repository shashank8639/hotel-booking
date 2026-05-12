package com.hotelbooking.exception;

import org.springframework.http.HttpStatus;

public class PaymentValidationException extends ApiException {
    public PaymentValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
