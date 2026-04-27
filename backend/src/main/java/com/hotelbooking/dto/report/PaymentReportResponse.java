package com.hotelbooking.dto.report;

import com.hotelbooking.database.PaymentStatus;
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
public class PaymentReportResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    /** When status filter is set, this is that status; otherwise null (all statuses). */
    private PaymentStatus statusFilter;
    private BigDecimal totalCollected;
    private BigDecimal totalRefunded;
    private long paymentCount;
    @Builder.Default
    private List<LabeledAmountDto> byStatus = new ArrayList<>();
    @Builder.Default
    private List<LabeledAmountDto> dailySeries = new ArrayList<>();
}
