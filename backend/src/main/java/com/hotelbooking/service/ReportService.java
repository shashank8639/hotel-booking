package com.hotelbooking.service;

import com.hotelbooking.database.BookingStatus;
import com.hotelbooking.database.PaymentStatus;
import com.hotelbooking.dto.report.BookingReportResponse;
import com.hotelbooking.dto.report.DashboardSummaryResponse;
import com.hotelbooking.dto.report.MonthlyReportResponse;
import com.hotelbooking.dto.report.OccupancyReportResponse;
import com.hotelbooking.dto.report.PaymentReportResponse;
import com.hotelbooking.dto.report.ReportPeriod;
import com.hotelbooking.dto.report.RevenueReportResponse;

import java.time.LocalDate;

public interface ReportService {

    DashboardSummaryResponse getDashboardSummary();

    RevenueReportResponse getRevenueReport(LocalDate startDate, LocalDate endDate, ReportPeriod period);

    OccupancyReportResponse getOccupancyReport(LocalDate startDate, LocalDate endDate);

    BookingReportResponse getBookingReport(LocalDate startDate, LocalDate endDate);

    MonthlyReportResponse getMonthlyReport(int year, int month);

    RevenueReportResponse getRevenueByDateRange(LocalDate startDate, LocalDate endDate);

    BookingReportResponse getBookingsByStatus(LocalDate startDate, LocalDate endDate, BookingStatus status);

    PaymentReportResponse getPaymentReport(LocalDate startDate, LocalDate endDate, PaymentStatus status);

    /**
     * Monthly report as CSV bytes (UTF-8). Controller returns raw bytes only.
     */
    byte[] exportMonthlyReportCsv(int year, int month);
}
