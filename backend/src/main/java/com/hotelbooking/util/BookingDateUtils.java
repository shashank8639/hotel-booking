package com.hotelbooking.util;

import com.hotelbooking.exception.InvalidBookingDatesException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Shared date rules for the booking engine.
 */
public final class BookingDateUtils {

    private BookingDateUtils() {
    }

    /**
     * Validates date order, minimum one night, and that check-in is not in the past.
     *
     * @return number of nights (checkOut - checkIn)
     */
    public static int validateAndCalculateNights(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new InvalidBookingDatesException("Check-in and check-out dates are required");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new InvalidBookingDatesException("Check-in date cannot be in the past");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new InvalidBookingDatesException("Check-out date must be after check-in date");
        }
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights < 1) {
            throw new InvalidBookingDatesException("Booking must be for at least one night");
        }
        return (int) nights;
    }
}
