package com.hotelbooking.controller;

import com.hotelbooking.database.BookingStatus;
import com.hotelbooking.dto.report.BookingReportResponse;
import com.hotelbooking.dto.report.DashboardSummaryResponse;
import com.hotelbooking.dto.report.MonthlyReportResponse;
import com.hotelbooking.dto.report.OccupancyReportResponse;
import com.hotelbooking.dto.report.PaymentReportResponse;
import com.hotelbooking.dto.report.RevenueReportResponse;
import com.hotelbooking.exception.GlobalExceptionHandler;
import com.hotelbooking.exception.InvalidReportFilterException;
import com.hotelbooking.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminReportControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReportService reportService;

    @InjectMocks
    private AdminReportController adminReportController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminReportController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(
                        new ByteArrayHttpMessageConverter(),
                        new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Test
    void dashboard_shouldReturn200() throws Exception {
        when(reportService.getDashboardSummary()).thenReturn(
                DashboardSummaryResponse.builder().totalGuests(10).totalRooms(5).build()
        );

        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalGuests").value(10));
    }

    @Test
    void revenue_shouldReturn200() throws Exception {
        when(reportService.getRevenueReport(any(), any(), any())).thenReturn(
                RevenueReportResponse.builder()
                        .totalRevenue(new BigDecimal("1000"))
                        .netRevenue(new BigDecimal("900"))
                        .build()
        );

        mockMvc.perform(get("/admin/reports/revenue")
                        .param("startDate", "2026-08-01")
                        .param("endDate", "2026-08-31")
                        .param("period", "DAILY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue").value(1000));
    }

    @Test
    void revenue_shouldReturn400OnInvalidFilter() throws Exception {
        when(reportService.getRevenueReport(any(), any(), any()))
                .thenThrow(new InvalidReportFilterException("startDate must be on or before endDate"));

        mockMvc.perform(get("/admin/reports/revenue")
                        .param("startDate", "2026-08-31")
                        .param("endDate", "2026-08-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void occupancy_shouldReturn200() throws Exception {
        when(reportService.getOccupancyReport(any(), any())).thenReturn(
                OccupancyReportResponse.builder()
                        .currentOccupancyPercent(new BigDecimal("25.00"))
                        .build()
        );

        mockMvc.perform(get("/admin/reports/occupancy")
                        .param("startDate", "2026-08-01")
                        .param("endDate", "2026-08-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentOccupancyPercent").value(25.00));
    }

    @Test
    void bookings_shouldReturn200() throws Exception {
        when(reportService.getBookingReport(any(), any())).thenReturn(
                BookingReportResponse.builder().totalBookings(8).build()
        );

        mockMvc.perform(get("/admin/reports/bookings")
                        .param("startDate", "2026-08-01")
                        .param("endDate", "2026-08-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBookings").value(8));
    }

    @Test
    void monthly_shouldReturn200() throws Exception {
        when(reportService.getMonthlyReport(2026, 8)).thenReturn(
                MonthlyReportResponse.builder().year(2026).month(8).monthlyBookings(12).build()
        );

        mockMvc.perform(get("/admin/reports/monthly")
                        .param("year", "2026")
                        .param("month", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyBookings").value(12));
    }

    @Test
    void payments_shouldReturn200() throws Exception {
        when(reportService.getPaymentReport(any(), any(), isNull())).thenReturn(
                PaymentReportResponse.builder()
                        .totalCollected(new BigDecimal("5000"))
                        .build()
        );

        mockMvc.perform(get("/admin/reports/payments")
                        .param("startDate", "2026-08-01")
                        .param("endDate", "2026-08-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCollected").value(5000));
    }

    @Test
    void bookingsStatus_shouldReturn200() throws Exception {
        when(reportService.getBookingsByStatus(any(), any(), eq(BookingStatus.CANCELLED))).thenReturn(
                BookingReportResponse.builder().cancelledBookings(3).build()
        );

        mockMvc.perform(get("/admin/reports/bookings/status")
                        .param("startDate", "2026-08-01")
                        .param("endDate", "2026-08-31")
                        .param("status", "CANCELLED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cancelledBookings").value(3));
    }

    @Test
    void revenueDateRange_shouldReturn200() throws Exception {
        when(reportService.getRevenueByDateRange(any(), any())).thenReturn(
                RevenueReportResponse.builder().startDate(LocalDate.of(2026, 8, 1)).build()
        );

        mockMvc.perform(get("/admin/reports/revenue/date-range")
                        .param("startDate", "2026-08-01")
                        .param("endDate", "2026-08-15"))
                .andExpect(status().isOk());
    }

    @Test
    void monthlyExport_shouldReturnCsvBytes() throws Exception {
        when(reportService.exportMonthlyReportCsv(2026, 8))
                .thenReturn("metric,value\nyear,2026\n".getBytes());

        mockMvc.perform(get("/admin/reports/monthly/export")
                        .param("year", "2026")
                        .param("month", "8"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("monthly-report-2026-08.csv")))
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("year,2026")));
    }

    @Test
    void payments_failedOnly_shouldPassStatus() throws Exception {
        when(reportService.getPaymentReport(any(), any(), eq(com.hotelbooking.database.PaymentStatus.FAILED)))
                .thenReturn(PaymentReportResponse.builder()
                        .statusFilter(com.hotelbooking.database.PaymentStatus.FAILED)
                        .paymentCount(3)
                        .build());

        mockMvc.perform(get("/admin/reports/payments")
                        .param("startDate", "2026-08-01")
                        .param("endDate", "2026-08-31")
                        .param("status", "FAILED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusFilter").value("FAILED"))
                .andExpect(jsonPath("$.paymentCount").value(3));
    }
}
