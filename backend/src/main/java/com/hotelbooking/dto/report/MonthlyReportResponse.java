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
public class MonthlyReportResponse {
    private int year;
    private int month;
    private BigDecimal monthlyRevenue;
    private BigDecimal monthlyRefunds;
    private long monthlyBookings;
    private long monthlyCancellations;
    private long monthlyGuestRegistrations;
    private BigDecimal monthlyOccupancyPercent;
    private long successfulPayments;
    private long failedPayments;
    private long pendingPayments;
    @Builder.Default
    private List<LabeledAmountDto> paymentSummary = new ArrayList<>();
}
