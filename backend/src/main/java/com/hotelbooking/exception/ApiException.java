package com.hotelbooking.exception;

import org.springframework.http.HttpStatus;

/**
 * Base type for domain exceptions that map cleanly to HTTP responses.
 */
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;

    protected ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
