package com.hotelbooking.database;

public enum EmailOutboxStatus {
    PENDING,
    SENT,
    FAILED,
    DEAD,
    SUPPRESSED
}
