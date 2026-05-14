package com.hotelbooking.dto.report;

import com.hotelbooking.database.BookingStatus;
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
public class BookingReportResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private long totalBookings;
    private long cancelledBookings;
    private long completedBookings;
    private long upcomingBookings;
    /** Mean booking totalAmount in range (0 when no bookings). */
    private BigDecimal averageBookingValue;
    @Builder.Default
    private List<LabeledAmountDto> byStatus = new ArrayList<>();
    @Builder.Default
    private List<LabeledAmountDto> byGuest = new ArrayList<>();
    @Builder.Default
    private List<LabeledAmountDto> byRoom = new ArrayList<>();
    @Builder.Default
    private List<LabeledAmountDto> byCheckInDate = new ArrayList<>();
}
