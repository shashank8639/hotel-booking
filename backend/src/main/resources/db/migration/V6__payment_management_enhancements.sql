-- Module 7: Payment Management enhancements (Razorpay fields, invoice, webhooks)
-- Renames COMPLETED → SUCCESS for gateway-aligned terminology.

ALTER TABLE payments DROP CHECK chk_payments_status;
ALTER TABLE payments DROP CHECK chk_payments_method;

UPDATE payments SET status = 'SUCCESS' WHERE status = 'COMPLETED';

ALTER TABLE payments
    ADD COLUMN refunded_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00 AFTER amount,
    ADD COLUMN currency VARCHAR(3) NOT NULL DEFAULT 'INR' AFTER refunded_amount,
    ADD COLUMN razorpay_order_id VARCHAR(100) NULL AFTER transaction_reference,
    ADD COLUMN razorpay_payment_id VARCHAR(100) NULL AFTER razorpay_order_id,
    ADD COLUMN invoice_number VARCHAR(50) NULL AFTER razorpay_payment_id,
    ADD COLUMN invoice_generated_at TIMESTAMP NULL AFTER invoice_number,
    ADD COLUMN failure_reason VARCHAR(500) NULL AFTER invoice_generated_at;

ALTER TABLE payments
    ADD CONSTRAINT chk_payments_status
        CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED', 'CANCELLED')),
    ADD CONSTRAINT chk_payments_method
        CHECK (payment_method IN ('CREDIT_CARD', 'DEBIT_CARD', 'UPI', 'CASH', 'BANK_TRANSFER', 'RAZORPAY')),
    ADD CONSTRAINT uk_payments_invoice_number UNIQUE (invoice_number);

CREATE INDEX idx_payments_razorpay_order_id ON payments (razorpay_order_id);
CREATE INDEX idx_payments_razorpay_payment_id ON payments (razorpay_payment_id);

CREATE TABLE IF NOT EXISTS payment_webhook_events (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    event_id        VARCHAR(100)    NOT NULL,
    event_type      VARCHAR(100)    NOT NULL,
    payload_hash    VARCHAR(64)     NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_payment_webhook_events PRIMARY KEY (id),
    CONSTRAINT uk_payment_webhook_event_id UNIQUE (event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
