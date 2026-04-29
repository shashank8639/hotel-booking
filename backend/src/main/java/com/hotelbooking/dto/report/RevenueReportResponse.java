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
public class RevenueReportResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private ReportPeriod period;
    private BigDecimal totalRevenue;
    private BigDecimal totalRefunds;
    private BigDecimal netRevenue;
    @Builder.Default
    private List<LabeledAmountDto> series = new ArrayList<>();
    @Builder.Default
    private List<LabeledAmountDto> byRoomType = new ArrayList<>();
    @Builder.Default
    private List<LabeledAmountDto> byBookingStatus = new ArrayList<>();
    @Builder.Default
    private List<LabeledAmountDto> byPaymentStatus = new ArrayList<>();
}
