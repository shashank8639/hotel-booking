package com.hotelbooking.exception;

import org.springframework.http.HttpStatus;

public class PaymentNotFoundException extends ApiException {
    public PaymentNotFoundException(Long id) {
        super("Payment not found with id: " + id, HttpStatus.NOT_FOUND);
    }

    public PaymentNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
