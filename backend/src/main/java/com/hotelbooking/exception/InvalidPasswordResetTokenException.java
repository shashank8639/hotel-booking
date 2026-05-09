package com.hotelbooking.exception;

import org.springframework.http.HttpStatus;

public class InvalidPasswordResetTokenException extends ApiException {

    public InvalidPasswordResetTokenException() {
        super("Invalid or expired password reset token", HttpStatus.BAD_REQUEST);
    }

    public InvalidPasswordResetTokenException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
