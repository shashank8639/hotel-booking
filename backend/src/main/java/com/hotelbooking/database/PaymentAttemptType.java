package com.hotelbooking.database;

/**
 * Lifecycle stage of a recorded payment attempt (audit / forensics).
 */
public enum PaymentAttemptType {
    CREATE_ORDER,
    VERIFY,
    WEBHOOK,
    REFUND,
    EXPIRE
}
