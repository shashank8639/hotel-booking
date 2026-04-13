package com.hotelbooking.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a room cannot be booked due to operational status
 * (e.g. MAINTENANCE, OUT_OF_SERVICE, OCCUPIED).
 */
public class RoomNotAvailableException extends ApiException {

    public RoomNotAvailableException(Long roomId, String roomNumber, String status) {
        super(
                "Room " + roomNumber + " (id=" + roomId + ") cannot be booked while status is " + status,
                HttpStatus.CONFLICT
        );
    }
}
