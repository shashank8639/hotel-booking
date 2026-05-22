package com.hotelbooking.database;

/**
 * Processing state of a payment transaction.
 */
public enum PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REFUNDED
}
