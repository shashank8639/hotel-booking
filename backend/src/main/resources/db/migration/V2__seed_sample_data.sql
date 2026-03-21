-- =============================================================================
-- Hotel Booking System — V2: Optional sample seed data for development
-- =============================================================================

INSERT INTO guests (first_name, last_name, email, phone, created_at, updated_at)
VALUES
    ('Rahul', 'Sharma', 'rahul.sharma@example.com', '+91-9876543210', NOW(), NOW()),
    ('Priya', 'Patel', 'priya.patel@example.com', '+91-9876543211', NOW(), NOW()),
    ('Amit', 'Kumar', 'amit.kumar@example.com', '+91-9876543212', NOW(), NOW());

INSERT INTO rooms (room_number, room_type, floor_number, capacity, price_per_night, status, description, created_at, updated_at)
VALUES
    ('101', 'STANDARD', 1, 2, 2500.00, 'AVAILABLE', 'Standard room with city view', NOW(), NOW()),
    ('102', 'STANDARD', 1, 2, 2500.00, 'AVAILABLE', 'Standard room with garden view', NOW(), NOW()),
    ('201', 'DELUXE', 2, 3, 4500.00, 'AVAILABLE', 'Deluxe room with mini bar', NOW(), NOW()),
    ('301', 'SUITE', 3, 4, 8500.00, 'AVAILABLE', 'Suite with living area', NOW(), NOW()),
    ('401', 'PRESIDENTIAL', 4, 6, 15000.00, 'MAINTENANCE', 'Presidential suite — under renovation', NOW(), NOW());

INSERT INTO bookings (guest_id, check_in_date, check_out_date, status, total_amount, special_requests, created_at, updated_at)
VALUES
    (1, '2026-08-01', '2026-08-05', 'CONFIRMED', 10000.00, 'Late check-in requested', NOW(), NOW()),
    (2, '2026-08-10', '2026-08-12', 'PENDING', 9000.00, 'Extra pillows', NOW(), NOW());

INSERT INTO booking_rooms (booking_id, room_id, price_per_night, number_of_nights, subtotal, created_at, updated_at)
VALUES
    (1, 1, 2500.00, 4, 10000.00, NOW(), NOW()),
    (2, 3, 4500.00, 2, 9000.00, NOW(), NOW());

INSERT INTO payments (booking_id, amount, payment_method, status, transaction_reference, paid_at, created_at, updated_at)
VALUES
    (1, 10000.00, 'CREDIT_CARD', 'COMPLETED', 'TXN-20260801-001', NOW(), NOW(), NOW()),
    (2, 4500.00, 'UPI', 'PENDING', 'TXN-20260810-002', NULL, NOW(), NOW());
