package com.hotelbooking.exception;

import org.springframework.http.HttpStatus;

public class InvoiceNotFoundException extends ApiException {
    public InvoiceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
