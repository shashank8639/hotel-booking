package com.hotelbooking.controller;

import com.hotelbooking.database.BookingStatus;
import com.hotelbooking.database.PaymentStatus;
import com.hotelbooking.dto.report.BookingReportResponse;
import com.hotelbooking.dto.report.DashboardSummaryResponse;
import com.hotelbooking.dto.report.MonthlyReportResponse;
import com.hotelbooking.dto.report.OccupancyReportResponse;
import com.hotelbooking.dto.report.PaymentReportResponse;
import com.hotelbooking.dto.report.ReportPeriod;
import com.hotelbooking.dto.report.RevenueReportResponse;
import com.hotelbooking.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Admin-only dashboard and reporting APIs.
 * Protected by SecurityConfig: {@code /admin/**} requires ROLE_ADMIN.
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Validated
@Tag(name = "Admin Reports", description = "Dashboard and analytics APIs for administrators")
public class AdminReportController {

    private final ReportService reportService;

    @Operation(summary = "Admin dashboard KPI summary")
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardSummaryResponse> dashboard() {
        return ResponseEntity.ok(reportService.getDashboardSummary());
    }

    @Operation(summary = "Revenue report with period buckets and breakdowns")
    @GetMapping("/reports/revenue")
    public ResponseEntity<RevenueReportResponse> revenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "DAILY") ReportPeriod period
    ) {
        return ResponseEntity.ok(reportService.getRevenueReport(startDate, endDate, period));
    }

    @Operation(summary = "Revenue report for an explicit date range (daily series)")
    @GetMapping("/reports/revenue/date-range")
    public ResponseEntity<RevenueReportResponse> revenueDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(reportService.getRevenueByDateRange(startDate, endDate));
    }

    @Operation(summary = "Occupancy and room utilization report")
    @GetMapping("/reports/occupancy")
    public ResponseEntity<OccupancyReportResponse> occupancy(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(reportService.getOccupancyReport(startDate, endDate));
    }

    @Operation(summary = "Booking analytics report")
    @GetMapping("/reports/bookings")
    public ResponseEntity<BookingReportResponse> bookings(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(reportService.getBookingReport(startDate, endDate));
    }

    @Operation(summary = "Bookings filtered/aggregated by status")
    @GetMapping("/reports/bookings/status")
    public ResponseEntity<BookingReportResponse> bookingsByStatus(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) BookingStatus status
    ) {
        return ResponseEntity.ok(reportService.getBookingsByStatus(startDate, endDate, status));
    }

    @Operation(summary = "Monthly consolidated report")
    @GetMapping("/reports/monthly")
    public ResponseEntity<MonthlyReportResponse> monthly(
            @RequestParam(required = false) @Min(2000) Integer year,
            @RequestParam(required = false) @Min(1) @Max(12) Integer month
    ) {
        YearMonth now = YearMonth.now();
        int y = year != null ? year : now.getYear();
        int m = month != null ? month : now.getMonthValue();
        return ResponseEntity.ok(reportService.getMonthlyReport(y, m));
    }

    @Operation(summary = "Export monthly report as CSV (raw bytes)")
    @GetMapping(value = "/reports/monthly/export", produces = "text/csv")
    public ResponseEntity<byte[]> monthlyCsv(
            @RequestParam(required = false) @Min(2000) Integer year,
            @RequestParam(required = false) @Min(1) @Max(12) Integer month
    ) {
        YearMonth now = YearMonth.now();
        int y = year != null ? year : now.getYear();
        int m = month != null ? month : now.getMonthValue();
        byte[] csv = reportService.exportMonthlyReportCsv(y, m);
        String filename = "monthly-report-" + y + "-" + String.format("%02d", m) + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(new MediaType("text", "csv"))
                .body(csv);
    }

    @Operation(summary = "Payment analytics report")
    @GetMapping("/reports/payments")
    public ResponseEntity<PaymentReportResponse> payments(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) PaymentStatus status
    ) {
        return ResponseEntity.ok(reportService.getPaymentReport(startDate, endDate, status));
    }
}
