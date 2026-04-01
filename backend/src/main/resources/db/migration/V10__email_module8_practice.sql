-- Module 8 Part F: email outbox, bounce suppressions, guest notification preferences

ALTER TABLE guests
    ADD COLUMN preferred_locale VARCHAR(10) NOT NULL DEFAULT 'en' AFTER phone,
    ADD COLUMN transactional_emails_enabled TINYINT(1) NOT NULL DEFAULT 1 AFTER preferred_locale,
    ADD COLUMN marketing_emails_enabled TINYINT(1) NOT NULL DEFAULT 0 AFTER transactional_emails_enabled;

CREATE TABLE IF NOT EXISTS email_outbox (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    to_address      VARCHAR(255)    NOT NULL,
    cc_address      VARCHAR(255)    NULL,
    subject         VARCHAR(500)    NOT NULL,
    template_name   VARCHAR(100)    NULL,
    email_type      VARCHAR(40)     NOT NULL,
    locale          VARCHAR(10)     NOT NULL DEFAULT 'en',
    body_html       MEDIUMTEXT      NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    attempt_count   INT             NOT NULL DEFAULT 0,
    last_error      VARCHAR(1000)   NULL,
    provider_message_id VARCHAR(255) NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    sent_at         TIMESTAMP       NULL,

    CONSTRAINT pk_email_outbox PRIMARY KEY (id),
    CONSTRAINT chk_email_outbox_status CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'DEAD', 'SUPPRESSED')),
    CONSTRAINT chk_email_outbox_type CHECK (email_type IN (
        'BOOKING_CONFIRMATION', 'BOOKING_CANCELLATION', 'PAYMENT_SUCCESS',
        'INVOICE', 'PASSWORD_RESET', 'MARKETING', 'OTHER'
    ))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_email_outbox_status_created ON email_outbox (status, created_at);
CREATE INDEX idx_email_outbox_to ON email_outbox (to_address);

CREATE TABLE IF NOT EXISTS email_suppressions (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    email           VARCHAR(255)    NOT NULL,
    reason          VARCHAR(40)     NOT NULL,
    detail          VARCHAR(1000)   NULL,
    active          TINYINT(1)      NOT NULL DEFAULT 1,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_email_suppressions PRIMARY KEY (id),
    CONSTRAINT uk_email_suppressions_email UNIQUE (email),
    CONSTRAINT chk_email_suppressions_reason CHECK (reason IN ('HARD_BOUNCE', 'SOFT_BOUNCE', 'COMPLAINT', 'MANUAL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
