-- =============================================================================
-- Hotel Booking System — V9: Module 7 practice stretch
-- PENDING expiry, GST exclusive columns, FX snapshot, payment_attempts audit
-- =============================================================================

ALTER TABLE payments
    ADD COLUMN expires_at TIMESTAMP NULL AFTER paid_at;

ALTER TABLE payments
    ADD COLUMN taxable_amount DECIMAL(12, 2) NULL AFTER amount;

ALTER TABLE payments
    ADD COLUMN gst_amount DECIMAL(12, 2) NULL AFTER taxable_amount;

ALTER TABLE payments
    ADD COLUMN base_currency CHAR(3) NOT NULL DEFAULT 'INR' AFTER currency;

ALTER TABLE payments
    ADD COLUMN fx_rate DECIMAL(18, 8) NULL AFTER base_currency;

ALTER TABLE payments
    ADD COLUMN amount_in_base DECIMAL(12, 2) NULL AFTER fx_rate;

CREATE INDEX idx_payments_pending_expiry ON payments (status, expires_at);

CREATE TABLE IF NOT EXISTS payment_attempts (
    id                BIGINT         NOT NULL AUTO_INCREMENT,
    payment_id        BIGINT         NULL,
    booking_id        BIGINT         NULL,
    attempt_type      VARCHAR(40)    NOT NULL,
    success           TINYINT(1)     NOT NULL DEFAULT 0,
    detail            VARCHAR(500)   NULL,
    request_fingerprint VARCHAR(128) NULL,
    created_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_payment_attempts PRIMARY KEY (id),
    CONSTRAINT fk_payment_attempts_payment FOREIGN KEY (payment_id) REFERENCES payments (id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_payment_attempts_booking FOREIGN KEY (booking_id) REFERENCES bookings (id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_payment_attempts_payment ON payment_attempts (payment_id);
CREATE INDEX idx_payment_attempts_type ON payment_attempts (attempt_type, created_at);
