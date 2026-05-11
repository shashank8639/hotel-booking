package com.hotelbooking.exception;

import org.springframework.http.HttpStatus;

public class InvalidPaymentSignatureException extends ApiException {
    public InvalidPaymentSignatureException() {
        super("Invalid Razorpay payment signature", HttpStatus.BAD_REQUEST);
    }
}
