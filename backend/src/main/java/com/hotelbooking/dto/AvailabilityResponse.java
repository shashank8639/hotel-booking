package com.hotelbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Availability check result for a date range (optionally scoped to room IDs).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilityResponse {

    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer numberOfNights;
    private List<RoomAvailabilityItem> rooms;
}
