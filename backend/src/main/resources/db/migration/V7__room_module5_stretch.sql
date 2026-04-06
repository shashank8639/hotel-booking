-- =============================================================================
-- Hotel Booking System — V7: Module 5 stretch (soft-delete, currency, CLEANING,
-- FULLTEXT description, seasonal prices)
-- =============================================================================

ALTER TABLE rooms
    ADD COLUMN currency CHAR(3) NOT NULL DEFAULT 'INR' AFTER discounted_price;

ALTER TABLE rooms
    ADD COLUMN deleted TINYINT(1) NOT NULL DEFAULT 0 AFTER description;

ALTER TABLE rooms
    DROP CHECK chk_rooms_status;

ALTER TABLE rooms
    ADD CONSTRAINT chk_rooms_status
        CHECK (status IN (
            'AVAILABLE', 'RESERVED', 'OCCUPIED', 'CLEANING', 'MAINTENANCE', 'OUT_OF_SERVICE'
        ));

-- Keyword / full-text search support on MySQL 8
CREATE FULLTEXT INDEX ft_rooms_description ON rooms (description);

CREATE INDEX idx_rooms_floor_number ON rooms (floor_number);
CREATE INDEX idx_rooms_deleted ON rooms (deleted);

-- Seasonal / date-range nightly rates (design + schema; resolve at booking time)
CREATE TABLE IF NOT EXISTS seasonal_room_prices (
    id                BIGINT         NOT NULL AUTO_INCREMENT,
    room_id           BIGINT         NOT NULL,
    season_label      VARCHAR(100)   NULL,
    start_date        DATE           NOT NULL,
    end_date          DATE           NOT NULL,
    price_per_night   DECIMAL(10, 2) NOT NULL,
    currency          CHAR(3)        NOT NULL DEFAULT 'INR',
    created_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_seasonal_room_prices PRIMARY KEY (id),
    CONSTRAINT fk_seasonal_prices_room FOREIGN KEY (room_id) REFERENCES rooms (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_seasonal_dates CHECK (end_date > start_date),
    CONSTRAINT chk_seasonal_price CHECK (price_per_night >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_seasonal_room_dates ON seasonal_room_prices (room_id, start_date, end_date);
