package com.hotelbooking.exception;

import org.springframework.http.HttpStatus;

public class DuplicateEmailException extends ApiException {

    public DuplicateEmailException(String email) {
        super("User already exists with email: " + email, HttpStatus.CONFLICT);
    }
}
