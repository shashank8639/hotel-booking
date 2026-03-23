-- =============================================================================
-- Hotel Booking System — Complete SQL Schema Reference
-- Mirrors Flyway V1__create_tables.sql for documentation and manual setup
-- =============================================================================

-- See: src/main/resources/db/migration/V1__create_tables.sql

-- Entity Relationship Overview:
--   guests (1) ──< (N) bookings (1) ──< (N) booking_rooms (N) >── (1) rooms
--   bookings (1) ──< (N) payments

-- Tables: guests, rooms, bookings, booking_rooms, payments
-- All monetary columns use DECIMAL(10,2) or DECIMAL(12,2)
-- All dates use DATE; audit timestamps use TIMESTAMP
