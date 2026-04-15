package com.hotelbooking.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a room has an overlapping non-cancelled booking for the requested dates.
 */
public class RoomAlreadyBookedException extends ApiException {

    public RoomAlreadyBookedException(Long roomId, String roomNumber) {
        super(
                "Room " + roomNumber + " (id=" + roomId + ") is already booked for the selected dates",
                HttpStatus.CONFLICT
        );
    }

    public RoomAlreadyBookedException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
