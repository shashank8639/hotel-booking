package com.hotelbooking.database;

/**
 * Processing state of a payment transaction.
 * <p>
 * {@code SUCCESS} replaces the earlier {@code COMPLETED} label to match gateway terminology.
 */
public enum PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED,
    REFUNDED,
    CANCELLED
}
