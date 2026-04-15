package com.hotelbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomAvailabilityDayItem {

    private LocalDate date;
    private boolean available;
    /** e.g. BOOKED, CLEANING, MAINTENANCE — null when available */
    private String blockReason;
}
