package com.hotelbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomAvailabilityCalendarResponse {

    private Long roomId;
    private String roomNumber;
    private LocalDate from;
    private LocalDate to;
    private List<RoomAvailabilityDayItem> days;
}
