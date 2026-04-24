package com.hotelbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Matrix: rows = rooms, columns = nights in [from, to).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomAvailabilityMatrixResponse {

    private LocalDate from;
    private LocalDate to;
    private int dayCount;
    private List<LocalDate> dates;
    private List<RoomAvailabilityMatrixRow> rooms;
}
