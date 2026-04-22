package com.hotelbooking.exception;

import org.springframework.http.HttpStatus;

public class InvalidReportFilterException extends ApiException {

    public InvalidReportFilterException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
