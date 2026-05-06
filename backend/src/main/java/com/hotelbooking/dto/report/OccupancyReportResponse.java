package com.hotelbooking.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OccupancyReportResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private long totalRooms;
    private long availableRooms;
    private long occupiedRooms;
    private long vacantRooms;
    private long reservedRooms;
    private long maintenanceRooms;
    /** Occupied / total * 100 for current snapshot. */
    private BigDecimal currentOccupancyPercent;
    /** Booked room-nights / (rooms * nights in range) * 100. */
    private BigDecimal periodOccupancyPercent;
    private long bookedRoomNights;
    private long capacityRoomNights;
    @Builder.Default
    private List<LabeledAmountDto> dailyOccupancy = new ArrayList<>();
}
