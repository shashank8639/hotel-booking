package com.hotelbooking.exception;

import org.springframework.http.HttpStatus;

public class DuplicatePaymentException extends ApiException {
    public DuplicatePaymentException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
