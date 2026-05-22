-- =============================================================================
-- Hotel Booking System — V1: Core persistence schema
-- Database: MySQL 8.x
-- =============================================================================

CREATE TABLE IF NOT EXISTS guests (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    first_name      VARCHAR(100)    NOT NULL,
    last_name       VARCHAR(100)    NOT NULL,
    email           VARCHAR(255)    NOT NULL,
    phone           VARCHAR(20)     NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_guests PRIMARY KEY (id),
    CONSTRAINT uk_guests_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_guests_last_name ON guests (last_name);


CREATE TABLE IF NOT EXISTS rooms (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    room_number     VARCHAR(20)     NOT NULL,
    room_type       VARCHAR(20)     NOT NULL,
    floor_number    INT             NULL,
    capacity        INT             NOT NULL,
    price_per_night DECIMAL(10, 2)  NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'AVAILABLE',
    description     TEXT            NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_rooms PRIMARY KEY (id),
    CONSTRAINT uk_rooms_room_number UNIQUE (room_number),
    CONSTRAINT chk_rooms_capacity CHECK (capacity > 0),
    CONSTRAINT chk_rooms_price CHECK (price_per_night >= 0),
    CONSTRAINT chk_rooms_status CHECK (status IN ('AVAILABLE', 'OCCUPIED', 'MAINTENANCE', 'OUT_OF_SERVICE')),
    CONSTRAINT chk_rooms_type CHECK (room_type IN ('STANDARD', 'DELUXE', 'SUITE', 'PRESIDENTIAL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_rooms_status ON rooms (status);
CREATE INDEX idx_rooms_type ON rooms (room_type);
CREATE INDEX idx_rooms_type_status ON rooms (room_type, status);


CREATE TABLE IF NOT EXISTS bookings (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    guest_id            BIGINT          NOT NULL,
    check_in_date       DATE            NOT NULL,
    check_out_date      DATE            NOT NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    total_amount        DECIMAL(12, 2)  NOT NULL DEFAULT 0.00,
    special_requests    TEXT            NULL,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_bookings PRIMARY KEY (id),
    CONSTRAINT fk_bookings_guest FOREIGN KEY (guest_id) REFERENCES guests (id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_bookings_dates CHECK (check_out_date > check_in_date),
    CONSTRAINT chk_bookings_total CHECK (total_amount >= 0),
    CONSTRAINT chk_bookings_status CHECK (status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_bookings_guest_id ON bookings (guest_id);
CREATE INDEX idx_bookings_status ON bookings (status);
CREATE INDEX idx_bookings_check_in ON bookings (check_in_date);
CREATE INDEX idx_bookings_check_out ON bookings (check_out_date);
CREATE INDEX idx_bookings_date_range ON bookings (check_in_date, check_out_date);


CREATE TABLE IF NOT EXISTS booking_rooms (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    booking_id          BIGINT          NOT NULL,
    room_id             BIGINT          NOT NULL,
    price_per_night     DECIMAL(10, 2)  NOT NULL,
    number_of_nights    INT             NOT NULL,
    subtotal            DECIMAL(12, 2)  NOT NULL,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_booking_rooms PRIMARY KEY (id),
    CONSTRAINT fk_booking_rooms_booking FOREIGN KEY (booking_id) REFERENCES bookings (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_booking_rooms_room FOREIGN KEY (room_id) REFERENCES rooms (id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT uk_booking_rooms_booking_room UNIQUE (booking_id, room_id),
    CONSTRAINT chk_booking_rooms_nights CHECK (number_of_nights > 0),
    CONSTRAINT chk_booking_rooms_price CHECK (price_per_night >= 0),
    CONSTRAINT chk_booking_rooms_subtotal CHECK (subtotal >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_booking_rooms_booking_id ON booking_rooms (booking_id);
CREATE INDEX idx_booking_rooms_room_id ON booking_rooms (room_id);


CREATE TABLE IF NOT EXISTS payments (
    id                      BIGINT          NOT NULL AUTO_INCREMENT,
    booking_id              BIGINT          NOT NULL,
    amount                  DECIMAL(12, 2)  NOT NULL,
    payment_method          VARCHAR(50)     NOT NULL,
    status                  VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    transaction_reference   VARCHAR(100)    NULL,
    paid_at                 TIMESTAMP       NULL,
    created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_payments PRIMARY KEY (id),
    CONSTRAINT fk_payments_booking FOREIGN KEY (booking_id) REFERENCES bookings (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT uk_payments_transaction_reference UNIQUE (transaction_reference),
    CONSTRAINT chk_payments_amount CHECK (amount > 0),
    CONSTRAINT chk_payments_status CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED')),
    CONSTRAINT chk_payments_method CHECK (payment_method IN ('CREDIT_CARD', 'DEBIT_CARD', 'UPI', 'CASH', 'BANK_TRANSFER'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_payments_booking_id ON payments (booking_id);
CREATE INDEX idx_payments_status ON payments (status);
CREATE INDEX idx_payments_paid_at ON payments (paid_at);
