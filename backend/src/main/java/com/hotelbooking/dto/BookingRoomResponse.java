package com.hotelbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Line-item view of a room within a booking (includes price snapshot).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRoomResponse {

    private Long id;
    private Long roomId;
    private String roomNumber;
    private BigDecimal pricePerNight;
    private Integer numberOfNights;
    private BigDecimal subtotal;
}
