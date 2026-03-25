-- =============================================================================
-- Hotel Booking System — V3: Optimistic locking + composite index
-- =============================================================================

ALTER TABLE bookings
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0 AFTER special_requests;

CREATE INDEX idx_booking_guest_status ON bookings (guest_id, status);
