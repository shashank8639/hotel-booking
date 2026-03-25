-- =============================================================================
-- Hotel Booking System — V5: Room management enhancements
-- discounted pricing, expanded types/statuses, room image metadata
-- =============================================================================

ALTER TABLE rooms
    ADD COLUMN discounted_price DECIMAL(10, 2) NULL AFTER price_per_night;

ALTER TABLE rooms
    DROP CHECK chk_rooms_status;

ALTER TABLE rooms
    ADD CONSTRAINT chk_rooms_status
        CHECK (status IN ('AVAILABLE', 'RESERVED', 'OCCUPIED', 'MAINTENANCE', 'OUT_OF_SERVICE'));

ALTER TABLE rooms
    DROP CHECK chk_rooms_type;

ALTER TABLE rooms
    ADD CONSTRAINT chk_rooms_type
        CHECK (room_type IN ('STANDARD', 'DELUXE', 'EXECUTIVE', 'SUITE', 'FAMILY', 'PRESIDENTIAL'));

ALTER TABLE rooms
    ADD CONSTRAINT chk_rooms_discounted_price
        CHECK (discounted_price IS NULL OR (discounted_price >= 0 AND discounted_price <= price_per_night));

CREATE TABLE IF NOT EXISTS room_images (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    room_id         BIGINT          NOT NULL,
    image_url       VARCHAR(500)    NOT NULL,
    caption         VARCHAR(255)    NULL,
    display_order   INT             NOT NULL DEFAULT 0,
    is_primary      TINYINT(1)      NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_room_images PRIMARY KEY (id),
    CONSTRAINT fk_room_images_room FOREIGN KEY (room_id) REFERENCES rooms (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_room_images_room_id ON room_images (room_id);
CREATE INDEX idx_rooms_price ON rooms (price_per_night);
CREATE INDEX idx_rooms_capacity ON rooms (capacity);
