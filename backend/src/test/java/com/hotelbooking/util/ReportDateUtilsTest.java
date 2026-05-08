package com.hotelbooking.util;

import com.hotelbooking.exception.InvalidReportFilterException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReportDateUtilsTest {

    @Test
    void validateRange_acceptsOrderedDates() {
        ReportDateUtils.validateRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));
    }

    @Test
    void validateRange_rejectsInvertedRange() {
        assertThatThrownBy(() ->
                ReportDateUtils.validateRange(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 1)))
                .isInstanceOf(InvalidReportFilterException.class)
                .hasMessageContaining("on or before");
    }

    @Test
    void validateRange_rejectsNull() {
        assertThatThrownBy(() -> ReportDateUtils.validateRange(null, LocalDate.now()))
                .isInstanceOf(InvalidReportFilterException.class);
    }

    @Test
    void endExclusive_isStartOfNextDay() {
        assertThat(ReportDateUtils.endExclusive(LocalDate.of(2026, 8, 6)))
                .isEqualTo(LocalDate.of(2026, 8, 7).atStartOfDay());
    }

    @Test
    void monthBounds_coverFullCalendarMonth() {
        YearMonth month = YearMonth.of(2026, 2);
        assertThat(ReportDateUtils.startOfMonth(month)).isEqualTo(LocalDate.of(2026, 2, 1).atStartOfDay());
        assertThat(ReportDateUtils.endOfMonthExclusive(month))
                .isEqualTo(LocalDate.of(2026, 3, 1).atStartOfDay());
    }

    @Test
    void nightsBetween_sameDayStayCountsAsOne() {
        LocalDate day = LocalDate.of(2026, 8, 6);
        assertThat(ReportDateUtils.nightsBetweenInclusiveStay(day, day)).isEqualTo(1L);
    }
}
