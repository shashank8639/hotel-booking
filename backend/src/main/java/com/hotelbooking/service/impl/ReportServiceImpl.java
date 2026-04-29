package com.hotelbooking.service.impl;

import com.hotelbooking.database.BookingStatus;
import com.hotelbooking.database.PaymentStatus;
import com.hotelbooking.database.RoomStatus;
import com.hotelbooking.dto.report.BookingReportResponse;
import com.hotelbooking.dto.report.DashboardSummaryResponse;
import com.hotelbooking.dto.report.LabeledAmountDto;
import com.hotelbooking.dto.report.MonthlyReportResponse;
import com.hotelbooking.dto.report.OccupancyReportResponse;
import com.hotelbooking.dto.report.PaymentReportResponse;
import com.hotelbooking.dto.report.RecentBookingDto;
import com.hotelbooking.dto.report.ReportPeriod;
import com.hotelbooking.dto.report.RevenueReportResponse;
import com.hotelbooking.entity.Booking;
import com.hotelbooking.exception.InvalidReportFilterException;
import com.hotelbooking.repository.ReportQueryRepository;
import com.hotelbooking.service.ReportService;
import com.hotelbooking.util.ReportDateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    /** Max inclusive calendar days for occupancy daily series (avoids huge loops). */
    static final int MAX_OCCUPANCY_INCLUSIVE_DAYS = 90;

    private final ReportQueryRepository reportQueryRepository;

    @Override
    public DashboardSummaryResponse getDashboardSummary() {
        log.info("Dashboard summary requested");
        LocalDate today = LocalDate.now();
        YearMonth month = YearMonth.from(today);

        long totalRooms = reportQueryRepository.countAllRooms();
        long available = reportQueryRepository.countRoomsByStatus(RoomStatus.AVAILABLE);
        long occupied = reportQueryRepository.countRoomsByStatus(RoomStatus.OCCUPIED);

        List<RecentBookingDto> recent = reportQueryRepository.findRecentBookings(5).stream()
                .map(this::toRecent)
                .toList();

        return DashboardSummaryResponse.builder()
                .totalGuests(reportQueryRepository.countAllGuests())
                .totalRooms(totalRooms)
                .availableRooms(available)
                .occupiedRooms(occupied)
                .todaysBookings(reportQueryRepository.countBookingsCreatedBetween(
                        ReportDateUtils.startOfDay(today), ReportDateUtils.endExclusive(today)))
                .todaysCheckIns(reportQueryRepository.countCheckInsOn(today))
                .todaysCheckOuts(reportQueryRepository.countCheckOutsOn(today))
                .todaysRevenue(reportQueryRepository.sumPaymentsByStatusBetween(
                        PaymentStatus.SUCCESS,
                        ReportDateUtils.startOfDay(today),
                        ReportDateUtils.endExclusive(today)))
                .monthlyRevenue(reportQueryRepository.sumPaymentsByStatusBetween(
                        PaymentStatus.SUCCESS,
                        ReportDateUtils.startOfMonth(month),
                        ReportDateUtils.endOfMonthExclusive(month)))
                .pendingPayments(reportQueryRepository.countPaymentsByStatus(PaymentStatus.PENDING))
                .completedPayments(reportQueryRepository.countPaymentsByStatus(PaymentStatus.SUCCESS))
                .cancelledBookings(reportQueryRepository.countBookingsByStatus(BookingStatus.CANCELLED))
                .recentBookings(recent)
                .build();
    }

    @Override
    public RevenueReportResponse getRevenueReport(LocalDate startDate, LocalDate endDate, ReportPeriod period) {
        log.info("Revenue report requested start={}, end={}, period={}", startDate, endDate, period);
        ReportDateUtils.validateRange(startDate, endDate);
        if (period == null) {
            period = ReportPeriod.DAILY;
        }

        LocalDateTime from = ReportDateUtils.startOfDay(startDate);
        LocalDateTime to = ReportDateUtils.endExclusive(endDate);

        BigDecimal total = reportQueryRepository.sumPaymentsByStatusBetween(PaymentStatus.SUCCESS, from, to);
        BigDecimal refunds = reportQueryRepository.sumRefundsBetween(from, to);

        List<LabeledAmountDto> series = switch (period) {
            case DAILY -> reportQueryRepository.revenueByDay(from, to);
            case WEEKLY -> toWeeklyBuckets(reportQueryRepository.revenueByDay(from, to));
            case MONTHLY, YEARLY -> reportQueryRepository.revenueByMonth(from, to);
        };

        return RevenueReportResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .period(period)
                .totalRevenue(total)
                .totalRefunds(refunds)
                .netRevenue(total.subtract(refunds))
                .series(series)
                .byRoomType(reportQueryRepository.revenueByRoomType(from, to))
                .byBookingStatus(reportQueryRepository.revenueByBookingStatus(from, to))
                .byPaymentStatus(reportQueryRepository.revenueByPaymentStatus(from, to))
                .build();
    }

    @Override
    public OccupancyReportResponse getOccupancyReport(LocalDate startDate, LocalDate endDate) {
        log.info("Occupancy report requested start={}, end={}", startDate, endDate);
        ReportDateUtils.validateMaxInclusiveDays(startDate, endDate, MAX_OCCUPANCY_INCLUSIVE_DAYS);

        long totalRooms = reportQueryRepository.countAllRooms();
        long available = reportQueryRepository.countRoomsByStatus(RoomStatus.AVAILABLE);
        long occupied = reportQueryRepository.countRoomsByStatus(RoomStatus.OCCUPIED);
        long reserved = reportQueryRepository.countRoomsByStatus(RoomStatus.RESERVED);
        long maintenance = reportQueryRepository.countRoomsByStatus(RoomStatus.MAINTENANCE)
                + reportQueryRepository.countRoomsByStatus(RoomStatus.OUT_OF_SERVICE);

        BigDecimal currentPct = percent(occupied, totalRooms);

        // API endDate is inclusive (same as revenue/booking filters). Internally use half-open [start, endExclusive).
        LocalDate endExclusive = endDate.plusDays(1);
        long nights = ChronoUnit.DAYS.between(startDate, endExclusive);
        long capacityRoomNights = totalRooms * nights;
        long bookedRoomNights = calculateBookedRoomNights(startDate, endExclusive);
        BigDecimal periodPct = percent(bookedRoomNights, capacityRoomNights);

        List<LabeledAmountDto> daily = buildDailyOccupancySeries(startDate, endExclusive, totalRooms);

        return OccupancyReportResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalRooms(totalRooms)
                .availableRooms(available)
                .occupiedRooms(occupied)
                .vacantRooms(available)
                .reservedRooms(reserved)
                .maintenanceRooms(maintenance)
                .currentOccupancyPercent(currentPct)
                .periodOccupancyPercent(periodPct)
                .bookedRoomNights(bookedRoomNights)
                .capacityRoomNights(capacityRoomNights)
                .dailyOccupancy(daily)
                .build();
    }

    @Override
    public BookingReportResponse getBookingReport(LocalDate startDate, LocalDate endDate) {
        log.info("Booking report requested start={}, end={}", startDate, endDate);
        ReportDateUtils.validateRange(startDate, endDate);
        LocalDateTime from = ReportDateUtils.startOfDay(startDate);
        LocalDateTime to = ReportDateUtils.endExclusive(endDate);

        long total = reportQueryRepository.countBookingsCreatedBetween(from, to);
        long cancelled = reportQueryRepository.countBookingsByStatusBetween(BookingStatus.CANCELLED, from, to);
        long completed = reportQueryRepository.countCompletedBookingsBetween(from, to);
        BigDecimal bookingTotals = reportQueryRepository.sumBookingTotalsCreatedBetween(from, to);
        BigDecimal averageBookingValue = total == 0
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : bookingTotals.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);

        return BookingReportResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalBookings(total)
                .cancelledBookings(cancelled)
                .completedBookings(completed)
                .upcomingBookings(reportQueryRepository.countUpcomingBookings(LocalDate.now()))
                .averageBookingValue(averageBookingValue)
                .byStatus(reportQueryRepository.bookingsByStatusBetween(from, to))
                .byGuest(reportQueryRepository.bookingsByGuest(from, to, 10))
                .byRoom(reportQueryRepository.bookingsByRoom(from, to, 10))
                .byCheckInDate(reportQueryRepository.bookingsByCheckInDate(startDate, endDate))
                .build();
    }

    @Override
    public MonthlyReportResponse getMonthlyReport(int year, int month) {
        log.info("Monthly report requested year={}, month={}", year, month);
        if (month < 1 || month > 12) {
            throw new InvalidReportFilterException("month must be between 1 and 12");
        }
        if (year < 2000 || year > LocalDate.now().getYear() + 1) {
            throw new InvalidReportFilterException("year is out of supported range");
        }

        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime from = ReportDateUtils.startOfMonth(ym);
        LocalDateTime to = ReportDateUtils.endOfMonthExclusive(ym);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        OccupancyReportResponse occupancy = getOccupancyReport(start, end);

        List<LabeledAmountDto> paymentSummary = reportQueryRepository.revenueByPaymentStatus(from, to);

        return MonthlyReportResponse.builder()
                .year(year)
                .month(month)
                .monthlyRevenue(reportQueryRepository.sumPaymentsByStatusBetween(PaymentStatus.SUCCESS, from, to))
                .monthlyRefunds(reportQueryRepository.sumRefundsBetween(from, to))
                .monthlyBookings(reportQueryRepository.countBookingsCreatedBetween(from, to))
                .monthlyCancellations(reportQueryRepository.countBookingsByStatusBetween(BookingStatus.CANCELLED, from, to))
                .monthlyGuestRegistrations(reportQueryRepository.countGuestsCreatedBetween(from, to))
                .monthlyOccupancyPercent(occupancy.getPeriodOccupancyPercent())
                .successfulPayments(reportQueryRepository.countPaymentsByStatusBetween(PaymentStatus.SUCCESS, from, to))
                .failedPayments(reportQueryRepository.countPaymentsByStatusBetween(PaymentStatus.FAILED, from, to))
                .pendingPayments(reportQueryRepository.countPaymentsByStatusBetween(PaymentStatus.PENDING, from, to))
                .paymentSummary(paymentSummary)
                .build();
    }

    @Override
    public RevenueReportResponse getRevenueByDateRange(LocalDate startDate, LocalDate endDate) {
        return getRevenueReport(startDate, endDate, ReportPeriod.DAILY);
    }

    @Override
    public BookingReportResponse getBookingsByStatus(LocalDate startDate, LocalDate endDate, BookingStatus status) {
        log.info("Bookings-by-status report status={}", status);
        BookingReportResponse full = getBookingReport(startDate, endDate);
        if (status == null) {
            return full;
        }
        List<LabeledAmountDto> filtered = full.getByStatus().stream()
                .filter(item -> status.name().equals(item.getLabel()))
                .toList();
        full.setByStatus(filtered);
        full.setTotalBookings(filtered.stream().mapToLong(LabeledAmountDto::getCount).sum());
        return full;
    }

    @Override
    public PaymentReportResponse getPaymentReport(LocalDate startDate, LocalDate endDate, PaymentStatus status) {
        log.info("Payment report requested start={}, end={}, status={}", startDate, endDate, status);
        ReportDateUtils.validateRange(startDate, endDate);
        LocalDateTime from = ReportDateUtils.startOfDay(startDate);
        LocalDateTime to = ReportDateUtils.endExclusive(endDate);

        List<LabeledAmountDto> byStatus = reportQueryRepository.revenueByPaymentStatus(from, to);
        if (status != null) {
            byStatus = byStatus.stream().filter(i -> status.name().equals(i.getLabel())).toList();
        }

        BigDecimal collected;
        BigDecimal refunded;
        List<LabeledAmountDto> dailySeries;
        long paymentCount = byStatus.stream().mapToLong(LabeledAmountDto::getCount).sum();

        if (status == PaymentStatus.FAILED) {
            // FAILED-only: use grouped amounts (failed rows often have null paidAt)
            collected = byStatus.isEmpty() || byStatus.get(0).getAmount() == null
                    ? BigDecimal.ZERO
                    : byStatus.get(0).getAmount();
            refunded = BigDecimal.ZERO;
            dailySeries = reportQueryRepository.paymentsByDayForStatus(PaymentStatus.FAILED, from, to);
        } else if (status == null || status == PaymentStatus.SUCCESS) {
            collected = reportQueryRepository.sumPaymentsByStatusBetween(PaymentStatus.SUCCESS, from, to);
            refunded = reportQueryRepository.sumRefundsBetween(from, to);
            dailySeries = reportQueryRepository.revenueByDay(from, to);
        } else {
            collected = byStatus.isEmpty() || byStatus.get(0).getAmount() == null
                    ? BigDecimal.ZERO
                    : byStatus.get(0).getAmount();
            refunded = BigDecimal.ZERO;
            dailySeries = reportQueryRepository.paymentsByDayForStatus(status, from, to);
        }

        return PaymentReportResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .statusFilter(status)
                .totalCollected(collected)
                .totalRefunded(refunded)
                .paymentCount(paymentCount)
                .byStatus(byStatus)
                .dailySeries(dailySeries)
                .build();
    }

    @Override
    public byte[] exportMonthlyReportCsv(int year, int month) {
        MonthlyReportResponse report = getMonthlyReport(year, month);
        StringBuilder csv = new StringBuilder();
        csv.append("metric,value\n");
        csv.append("year,").append(report.getYear()).append('\n');
        csv.append("month,").append(report.getMonth()).append('\n');
        csv.append("monthlyRevenue,").append(nullToZero(report.getMonthlyRevenue())).append('\n');
        csv.append("monthlyRefunds,").append(nullToZero(report.getMonthlyRefunds())).append('\n');
        csv.append("monthlyBookings,").append(report.getMonthlyBookings()).append('\n');
        csv.append("monthlyCancellations,").append(report.getMonthlyCancellations()).append('\n');
        csv.append("monthlyGuestRegistrations,").append(report.getMonthlyGuestRegistrations()).append('\n');
        csv.append("monthlyOccupancyPercent,").append(nullToZero(report.getMonthlyOccupancyPercent())).append('\n');
        csv.append("successfulPayments,").append(report.getSuccessfulPayments()).append('\n');
        csv.append("failedPayments,").append(report.getFailedPayments()).append('\n');
        csv.append("pendingPayments,").append(report.getPendingPayments()).append('\n');
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Roll daily SUCCESS revenue into ISO week buckets (same {@code /reports/revenue} path, period=WEEKLY).
     */
    private List<LabeledAmountDto> toWeeklyBuckets(List<LabeledAmountDto> daily) {
        WeekFields weeks = WeekFields.ISO;
        Map<String, BigDecimal> amounts = new LinkedHashMap<>();
        Map<String, Long> counts = new LinkedHashMap<>();

        for (LabeledAmountDto day : daily) {
            String raw = day.getLabel();
            LocalDate date = LocalDate.parse(raw.length() >= 10 ? raw.substring(0, 10) : raw);
            int week = date.get(weeks.weekOfWeekBasedYear());
            int weekYear = date.get(weeks.weekBasedYear());
            String label = String.format(Locale.ROOT, "%d-W%02d", weekYear, week);
            amounts.merge(label, day.getAmount() == null ? BigDecimal.ZERO : day.getAmount(), BigDecimal::add);
            counts.merge(label, day.getCount(), Long::sum);
        }

        List<LabeledAmountDto> result = new ArrayList<>();
        for (String label : amounts.keySet()) {
            result.add(LabeledAmountDto.builder()
                    .label(label)
                    .amount(amounts.get(label))
                    .count(counts.getOrDefault(label, 0L))
                    .build());
        }
        return result;
    }

    private static BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private long calculateBookedRoomNights(LocalDate start, LocalDate end) {
        long total = 0;
        for (Object[] row : reportQueryRepository.bookingRoomStaysOverlapping(start, end)) {
            LocalDate checkIn = (LocalDate) row[0];
            LocalDate checkOut = (LocalDate) row[1];
            LocalDate overlapStart = checkIn.isAfter(start) ? checkIn : start;
            LocalDate overlapEnd = checkOut.isBefore(end) ? checkOut : end;
            long nights = ChronoUnit.DAYS.between(overlapStart, overlapEnd);
            if (nights > 0) {
                total += nights;
            }
        }
        return total;
    }

    private List<LabeledAmountDto> buildDailyOccupancySeries(LocalDate start, LocalDate end, long totalRooms) {
        List<LabeledAmountDto> series = new ArrayList<>();
        if (totalRooms == 0) {
            return series;
        }
        for (LocalDate day = start; day.isBefore(end); day = day.plusDays(1)) {
            long nights = calculateBookedRoomNights(day, day.plusDays(1));
            BigDecimal pct = percent(nights, totalRooms);
            series.add(LabeledAmountDto.builder()
                    .label(day.toString())
                    .amount(pct)
                    .count(nights)
                    .build());
        }
        return series;
    }

    private static BigDecimal percent(long numerator, long denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    private RecentBookingDto toRecent(Booking booking) {
        return RecentBookingDto.builder()
                .id(booking.getId())
                .guestName(booking.getGuest().getFirstName() + " " + booking.getGuest().getLastName())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .status(booking.getStatus())
                .totalAmount(booking.getTotalAmount())
                .build();
    }
}
