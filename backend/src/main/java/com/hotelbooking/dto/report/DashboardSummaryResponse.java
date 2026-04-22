package com.hotelbooking.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryResponse {
    private long totalGuests;
    private long totalRooms;
    private long availableRooms;
    private long occupiedRooms;
    private long todaysBookings;
    private long todaysCheckIns;
    private long todaysCheckOuts;
    private BigDecimal todaysRevenue;
    private BigDecimal monthlyRevenue;
    private long pendingPayments;
    private long completedPayments;
    private long cancelledBookings;
    @Builder.Default
    private List<RecentBookingDto> recentBookings = new ArrayList<>();
}
