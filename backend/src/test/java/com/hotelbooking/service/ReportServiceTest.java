package com.hotelbooking.service;

import com.hotelbooking.database.BookingStatus;
import com.hotelbooking.database.PaymentStatus;
import com.hotelbooking.database.RoomStatus;
import com.hotelbooking.dto.report.BookingReportResponse;
import com.hotelbooking.dto.report.DashboardSummaryResponse;
import com.hotelbooking.dto.report.LabeledAmountDto;
import com.hotelbooking.dto.report.MonthlyReportResponse;
import com.hotelbooking.dto.report.OccupancyReportResponse;
import com.hotelbooking.dto.report.ReportPeriod;
import com.hotelbooking.dto.report.RevenueReportResponse;
import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.Guest;
import com.hotelbooking.exception.InvalidReportFilterException;
import com.hotelbooking.repository.ReportQueryRepository;
import com.hotelbooking.service.impl.ReportServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportQueryRepository reportQueryRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    @Test
    void dashboard_shouldAggregateKpis() {
        when(reportQueryRepository.countAllGuests()).thenReturn(20L);
        when(reportQueryRepository.countAllRooms()).thenReturn(10L);
        when(reportQueryRepository.countRoomsByStatus(RoomStatus.AVAILABLE)).thenReturn(6L);
        when(reportQueryRepository.countRoomsByStatus(RoomStatus.OCCUPIED)).thenReturn(3L);
        when(reportQueryRepository.countBookingsCreatedBetween(any(), any())).thenReturn(2L);
        when(reportQueryRepository.countCheckInsOn(any())).thenReturn(1L);
        when(reportQueryRepository.countCheckOutsOn(any())).thenReturn(1L);
        when(reportQueryRepository.sumPaymentsByStatusBetween(eq(PaymentStatus.SUCCESS), any(), any()))
                .thenReturn(new BigDecimal("5000.00"));
        when(reportQueryRepository.countPaymentsByStatus(PaymentStatus.PENDING)).thenReturn(4L);
        when(reportQueryRepository.countPaymentsByStatus(PaymentStatus.SUCCESS)).thenReturn(15L);
        when(reportQueryRepository.countBookingsByStatus(BookingStatus.CANCELLED)).thenReturn(3L);
        when(reportQueryRepository.findRecentBookings(5)).thenReturn(List.of(sampleBooking()));

        DashboardSummaryResponse dashboard = reportService.getDashboardSummary();

        assertThat(dashboard.getTotalGuests()).isEqualTo(20);
        assertThat(dashboard.getAvailableRooms()).isEqualTo(6);
        assertThat(dashboard.getOccupiedRooms()).isEqualTo(3);
        assertThat(dashboard.getRecentBookings()).hasSize(1);
        assertThat(dashboard.getTodaysRevenue()).isEqualByComparingTo("5000.00");
    }

    @Test
    void revenueReport_shouldRejectInvalidDates() {
        assertThatThrownBy(() -> reportService.getRevenueReport(
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 1),
                ReportPeriod.DAILY
        )).isInstanceOf(InvalidReportFilterException.class);
    }

    @Test
    void revenueReport_shouldComputeNet() {
        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 31);
        when(reportQueryRepository.sumPaymentsByStatusBetween(eq(PaymentStatus.SUCCESS), any(), any()))
                .thenReturn(new BigDecimal("10000"));
        when(reportQueryRepository.sumRefundsBetween(any(), any())).thenReturn(new BigDecimal("1000"));
        when(reportQueryRepository.revenueByDay(any(), any())).thenReturn(List.of(
                LabeledAmountDto.builder().label("2026-08-01").amount(new BigDecimal("10000")).count(2L).build()
        ));
        when(reportQueryRepository.revenueByRoomType(any(), any())).thenReturn(List.of());
        when(reportQueryRepository.revenueByBookingStatus(any(), any())).thenReturn(List.of());
        when(reportQueryRepository.revenueByPaymentStatus(any(), any())).thenReturn(List.of());

        RevenueReportResponse report = reportService.getRevenueReport(start, end, ReportPeriod.DAILY);

        assertThat(report.getTotalRevenue()).isEqualByComparingTo("10000");
        assertThat(report.getTotalRefunds()).isEqualByComparingTo("1000");
        assertThat(report.getNetRevenue()).isEqualByComparingTo("9000");
        assertThat(report.getSeries()).hasSize(1);
    }

    @Test
    void occupancyReport_shouldComputePercent() {
        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 2); // inclusive → 2 calendar nights (Aug 1–2)
        when(reportQueryRepository.countAllRooms()).thenReturn(10L);
        when(reportQueryRepository.countRoomsByStatus(RoomStatus.AVAILABLE)).thenReturn(7L);
        when(reportQueryRepository.countRoomsByStatus(RoomStatus.OCCUPIED)).thenReturn(2L);
        when(reportQueryRepository.countRoomsByStatus(RoomStatus.RESERVED)).thenReturn(1L);
        when(reportQueryRepository.countRoomsByStatus(RoomStatus.MAINTENANCE)).thenReturn(0L);
        when(reportQueryRepository.countRoomsByStatus(RoomStatus.OUT_OF_SERVICE)).thenReturn(0L);
        when(reportQueryRepository.bookingRoomStaysOverlapping(any(), any())).thenReturn(
                java.util.Collections.singletonList(
                        new Object[]{LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 3), 2}
                )
        );

        OccupancyReportResponse report = reportService.getOccupancyReport(start, end);

        assertThat(report.getCurrentOccupancyPercent()).isEqualByComparingTo("20.00"); // 2/10
        assertThat(report.getCapacityRoomNights()).isEqualTo(20); // 10 rooms * 2 nights
        assertThat(report.getBookedRoomNights()).isEqualTo(2);
        assertThat(report.getPeriodOccupancyPercent()).isEqualByComparingTo("10.00");
    }

    @Test
    void bookingReport_shouldIncludeBreakdowns() {
        when(reportQueryRepository.countBookingsCreatedBetween(any(), any())).thenReturn(5L);
        when(reportQueryRepository.countBookingsByStatusBetween(eq(BookingStatus.CANCELLED), any(), any())).thenReturn(1L);
        when(reportQueryRepository.countCompletedBookingsBetween(any(), any())).thenReturn(2L);
        when(reportQueryRepository.countUpcomingBookings(any())).thenReturn(3L);
        when(reportQueryRepository.sumBookingTotalsCreatedBetween(any(), any()))
                .thenReturn(new BigDecimal("12500.00"));
        when(reportQueryRepository.bookingsByStatusBetween(any(), any())).thenReturn(List.of());
        when(reportQueryRepository.bookingsByGuest(any(), any(), eq(10))).thenReturn(List.of());
        when(reportQueryRepository.bookingsByRoom(any(), any(), eq(10))).thenReturn(List.of());
        when(reportQueryRepository.bookingsByCheckInDate(any(), any())).thenReturn(List.of());

        BookingReportResponse report = reportService.getBookingReport(
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31));

        assertThat(report.getTotalBookings()).isEqualTo(5);
        assertThat(report.getCancelledBookings()).isEqualTo(1);
        assertThat(report.getCompletedBookings()).isEqualTo(2);
        assertThat(report.getAverageBookingValue()).isEqualByComparingTo("2500.00");
    }

    @Test
    void occupancyReport_whenTotalRoomsZero_shouldReturnZeroPercentsAndEmptySeries() {
        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 3);
        when(reportQueryRepository.countAllRooms()).thenReturn(0L);
        when(reportQueryRepository.countRoomsByStatus(any())).thenReturn(0L);
        when(reportQueryRepository.bookingRoomStaysOverlapping(any(), any())).thenReturn(List.of());

        OccupancyReportResponse report = reportService.getOccupancyReport(start, end);

        assertThat(report.getTotalRooms()).isZero();
        assertThat(report.getCurrentOccupancyPercent()).isEqualByComparingTo("0.00");
        assertThat(report.getPeriodOccupancyPercent()).isEqualByComparingTo("0.00");
        assertThat(report.getCapacityRoomNights()).isZero();
        assertThat(report.getDailyOccupancy()).isEmpty();
    }

    @Test
    void occupancyReport_shouldRejectRangeOver90Days() {
        assertThatThrownBy(() -> reportService.getOccupancyReport(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 4, 15)
        ))
                .isInstanceOf(InvalidReportFilterException.class)
                .hasMessageContaining("90");
    }

    @Test
    void revenueReport_weekly_shouldBucketWithoutChangingPath() {
        LocalDate start = LocalDate.of(2026, 8, 3); // Monday
        LocalDate end = LocalDate.of(2026, 8, 9);   // Sunday same ISO week
        when(reportQueryRepository.sumPaymentsByStatusBetween(eq(PaymentStatus.SUCCESS), any(), any()))
                .thenReturn(new BigDecimal("300"));
        when(reportQueryRepository.sumRefundsBetween(any(), any())).thenReturn(BigDecimal.ZERO);
        when(reportQueryRepository.revenueByDay(any(), any())).thenReturn(List.of(
                LabeledAmountDto.builder().label("2026-08-03").amount(new BigDecimal("100")).count(1L).build(),
                LabeledAmountDto.builder().label("2026-08-05").amount(new BigDecimal("200")).count(2L).build()
        ));
        when(reportQueryRepository.revenueByRoomType(any(), any())).thenReturn(List.of());
        when(reportQueryRepository.revenueByBookingStatus(any(), any())).thenReturn(List.of());
        when(reportQueryRepository.revenueByPaymentStatus(any(), any())).thenReturn(List.of());

        RevenueReportResponse report = reportService.getRevenueReport(start, end, ReportPeriod.WEEKLY);

        assertThat(report.getPeriod()).isEqualTo(ReportPeriod.WEEKLY);
        assertThat(report.getSeries()).hasSize(1);
        assertThat(report.getSeries().get(0).getLabel()).isEqualTo("2026-W32");
        assertThat(report.getSeries().get(0).getAmount()).isEqualByComparingTo("300");
        assertThat(report.getSeries().get(0).getCount()).isEqualTo(3L);
    }

    @Test
    void paymentReport_failedOnly_shouldFilterStatusAndSeries() {
        when(reportQueryRepository.revenueByPaymentStatus(any(), any())).thenReturn(List.of(
                LabeledAmountDto.builder().label("FAILED").amount(new BigDecimal("900")).count(3L).build(),
                LabeledAmountDto.builder().label("SUCCESS").amount(new BigDecimal("5000")).count(10L).build()
        ));
        when(reportQueryRepository.paymentsByDayForStatus(eq(PaymentStatus.FAILED), any(), any())).thenReturn(List.of(
                LabeledAmountDto.builder().label("2026-08-01").amount(new BigDecimal("900")).count(3L).build()
        ));

        var report = reportService.getPaymentReport(
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31), PaymentStatus.FAILED);

        assertThat(report.getStatusFilter()).isEqualTo(PaymentStatus.FAILED);
        assertThat(report.getByStatus()).hasSize(1);
        assertThat(report.getByStatus().get(0).getLabel()).isEqualTo("FAILED");
        assertThat(report.getTotalCollected()).isEqualByComparingTo("900");
        assertThat(report.getTotalRefunded()).isEqualByComparingTo("0");
        assertThat(report.getPaymentCount()).isEqualTo(3);
        assertThat(report.getDailySeries()).hasSize(1);
    }

    @Test
    void exportMonthlyReportCsv_shouldReturnUtf8Bytes() {
        stubOccupancyDeps();
        when(reportQueryRepository.sumPaymentsByStatusBetween(eq(PaymentStatus.SUCCESS), any(), any()))
                .thenReturn(new BigDecimal("8000"));
        when(reportQueryRepository.sumRefundsBetween(any(), any())).thenReturn(BigDecimal.ZERO);
        when(reportQueryRepository.countBookingsCreatedBetween(any(), any())).thenReturn(12L);
        when(reportQueryRepository.countBookingsByStatusBetween(eq(BookingStatus.CANCELLED), any(), any())).thenReturn(2L);
        when(reportQueryRepository.countGuestsCreatedBetween(any(), any())).thenReturn(4L);
        when(reportQueryRepository.countPaymentsByStatusBetween(eq(PaymentStatus.SUCCESS), any(), any())).thenReturn(10L);
        when(reportQueryRepository.countPaymentsByStatusBetween(eq(PaymentStatus.FAILED), any(), any())).thenReturn(1L);
        when(reportQueryRepository.countPaymentsByStatusBetween(eq(PaymentStatus.PENDING), any(), any())).thenReturn(1L);
        when(reportQueryRepository.revenueByPaymentStatus(any(), any())).thenReturn(List.of());

        byte[] csv = reportService.exportMonthlyReportCsv(2026, 8);
        String text = new String(csv, java.nio.charset.StandardCharsets.UTF_8);

        assertThat(text).startsWith("metric,value");
        assertThat(text).contains("year,2026");
        assertThat(text).contains("month,8");
        assertThat(text).contains("monthlyBookings,12");
        assertThat(text).contains("monthlyRevenue,8000");
    }

    @Test
    void monthlyReport_shouldValidateMonth() {
        assertThatThrownBy(() -> reportService.getMonthlyReport(2026, 13))
                .isInstanceOf(InvalidReportFilterException.class);
    }

    @Test
    void monthlyReport_shouldReturnSummary() {
        stubOccupancyDeps();
        when(reportQueryRepository.sumPaymentsByStatusBetween(eq(PaymentStatus.SUCCESS), any(), any()))
                .thenReturn(new BigDecimal("8000"));
        when(reportQueryRepository.sumRefundsBetween(any(), any())).thenReturn(BigDecimal.ZERO);
        when(reportQueryRepository.countBookingsCreatedBetween(any(), any())).thenReturn(12L);
        when(reportQueryRepository.countBookingsByStatusBetween(eq(BookingStatus.CANCELLED), any(), any())).thenReturn(2L);
        when(reportQueryRepository.countGuestsCreatedBetween(any(), any())).thenReturn(4L);
        when(reportQueryRepository.countPaymentsByStatusBetween(eq(PaymentStatus.SUCCESS), any(), any())).thenReturn(10L);
        when(reportQueryRepository.countPaymentsByStatusBetween(eq(PaymentStatus.FAILED), any(), any())).thenReturn(1L);
        when(reportQueryRepository.countPaymentsByStatusBetween(eq(PaymentStatus.PENDING), any(), any())).thenReturn(1L);
        when(reportQueryRepository.revenueByPaymentStatus(any(), any())).thenReturn(List.of());

        MonthlyReportResponse report = reportService.getMonthlyReport(2026, 8);

        assertThat(report.getYear()).isEqualTo(2026);
        assertThat(report.getMonth()).isEqualTo(8);
        assertThat(report.getMonthlyBookings()).isEqualTo(12);
        assertThat(report.getMonthlyRevenue()).isEqualByComparingTo("8000");
    }

    private void stubOccupancyDeps() {
        when(reportQueryRepository.countAllRooms()).thenReturn(10L);
        when(reportQueryRepository.countRoomsByStatus(any())).thenReturn(0L);
        when(reportQueryRepository.countRoomsByStatus(RoomStatus.AVAILABLE)).thenReturn(10L);
        when(reportQueryRepository.bookingRoomStaysOverlapping(any(), any())).thenReturn(List.of());
    }

    private Booking sampleBooking() {
        Guest guest = Guest.builder().firstName("Asha").lastName("Patel").email("a@example.com").build();
        Booking booking = Booking.builder()
                .guest(guest)
                .checkInDate(LocalDate.now().plusDays(1))
                .checkOutDate(LocalDate.now().plusDays(2))
                .status(BookingStatus.CONFIRMED)
                .totalAmount(new BigDecimal("2500"))
                .build();
        booking.setId(99L);
        return booking;
    }
}
