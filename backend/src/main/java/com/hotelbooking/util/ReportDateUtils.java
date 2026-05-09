package com.hotelbooking.util;

import com.hotelbooking.exception.InvalidReportFilterException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

/**
 * Date-range helpers for reporting filters.
 */
public final class ReportDateUtils {

    private ReportDateUtils() {
    }

    public static void validateRange(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new InvalidReportFilterException("startDate and endDate are required");
        }
        if (end.isBefore(start)) {
            throw new InvalidReportFilterException("startDate must be on or before endDate");
        }
        if (start.isAfter(LocalDate.now().plusYears(5))) {
            throw new InvalidReportFilterException("startDate is unreasonably far in the future");
        }
    }

    /**
     * Inclusive calendar-day span must not exceed {@code maxInclusiveDays} (e.g. 90 for occupancy series).
     */
    public static void validateMaxInclusiveDays(LocalDate start, LocalDate end, int maxInclusiveDays) {
        validateRange(start, end);
        long days = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
        if (days > maxInclusiveDays) {
            throw new InvalidReportFilterException(
                    "Date range must not exceed " + maxInclusiveDays
                            + " days (requested " + days + " days)");
        }
    }

    public static LocalDateTime startOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    /** Exclusive end bound: start of the day after {@code end}. */
    public static LocalDateTime endExclusive(LocalDate end) {
        return end.plusDays(1).atStartOfDay();
    }

    public static LocalDateTime startOfMonth(YearMonth month) {
        return month.atDay(1).atStartOfDay();
    }

    public static LocalDateTime endOfMonthExclusive(YearMonth month) {
        return month.plusMonths(1).atDay(1).atStartOfDay();
    }

    public static long nightsBetweenInclusiveStay(LocalDate start, LocalDate end) {
        validateRange(start, end);
        long nights = java.time.temporal.ChronoUnit.DAYS.between(start, end);
        return Math.max(nights, 1);
    }
}
