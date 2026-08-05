package com.hotelbooking.exception;

import org.springframework.http.HttpStatus;

public class RoomImageNotFoundException extends ApiException {

    public RoomImageNotFoundException(Long imageId, Long roomId) {
        super("Room image not found with id " + imageId + " for room " + roomId, HttpStatus.NOT_FOUND);
    }
}
