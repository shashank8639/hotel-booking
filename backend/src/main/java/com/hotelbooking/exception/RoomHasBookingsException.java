package com.hotelbooking.exception;

import org.springframework.http.HttpStatus;

public class RoomHasBookingsException extends ApiException {

    public RoomHasBookingsException(Long roomId) {
        super("Cannot delete room with id " + roomId + " because booking history exists", HttpStatus.CONFLICT);
    }
}
