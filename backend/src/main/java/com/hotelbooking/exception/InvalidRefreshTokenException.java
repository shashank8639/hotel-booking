package com.hotelbooking.exception;

import org.springframework.http.HttpStatus;

public class InvalidRefreshTokenException extends ApiException {

    public InvalidRefreshTokenException() {
        super("Invalid or expired refresh token", HttpStatus.UNAUTHORIZED);
    }
}
