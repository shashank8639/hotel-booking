-- =============================================================================
-- Hotel Booking System — V8: Module 6 stretch
-- soft-hold expiry, overlap-friendly indexes
-- =============================================================================

-- Soft-hold: PENDING bookings expire if unpaid / unconfirmed
ALTER TABLE bookings
    ADD COLUMN hold_expires_at TIMESTAMP NULL AFTER special_requests;

CREATE INDEX idx_bookings_pending_hold ON bookings (status, hold_expires_at);

-- Overlap query path: room_id → booking dates + status
-- (booking_rooms already has idx_booking_rooms_room_id; this supports date predicates)
CREATE INDEX idx_bookings_status_dates ON bookings (status, check_in_date, check_out_date);

-- Covering helper for "my bookings" by guest + status + check-in
CREATE INDEX idx_bookings_guest_status_checkin ON bookings (guest_id, status, check_in_date);
