package com.hotelbooking.repository;

import com.hotelbooking.database.BookingStatus;
import com.hotelbooking.database.PaymentStatus;
import com.hotelbooking.database.RoomStatus;
import com.hotelbooking.dto.report.LabeledAmountDto;
import com.hotelbooking.entity.Booking;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Read-only aggregation queries for admin reporting / dashboard.
 * Kept separate from OLTP repositories to avoid bloating CRUD interfaces.
 */
@Repository
public class ReportQueryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public BigDecimal sumPaymentsByStatusBetween(PaymentStatus status, LocalDateTime from, LocalDateTime to) {
        BigDecimal result = entityManager.createQuery("""
                SELECT COALESCE(SUM(p.amount), 0)
                FROM Payment p
                WHERE p.status = :status
                  AND p.paidAt >= :from AND p.paidAt < :to
                """, BigDecimal.class)
                .setParameter("status", status)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();
        return result == null ? BigDecimal.ZERO : result;
    }

    public BigDecimal sumRefundsBetween(LocalDateTime from, LocalDateTime to) {
        BigDecimal result = entityManager.createQuery("""
                SELECT COALESCE(SUM(p.refundedAmount), 0)
                FROM Payment p
                WHERE p.updatedAt >= :from AND p.updatedAt < :to
                  AND p.refundedAmount > 0
                """, BigDecimal.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();
        return result == null ? BigDecimal.ZERO : result;
    }

    public List<LabeledAmountDto> revenueByPaymentStatus(LocalDateTime from, LocalDateTime to) {
        List<Object[]> rows = entityManager.createQuery("""
                SELECT p.status, COALESCE(SUM(p.amount), 0), COUNT(p)
                FROM Payment p
                WHERE p.createdAt >= :from AND p.createdAt < :to
                GROUP BY p.status
                ORDER BY p.status
                """, Object[].class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
        return mapEnumAmount(rows);
    }

    public List<LabeledAmountDto> revenueByDay(LocalDateTime from, LocalDateTime to) {
        // FUNCTION('DATE', ...) works in MySQL and H2 MySQL mode used by tests.
        List<Object[]> rows = entityManager.createQuery("""
                SELECT FUNCTION('DATE', p.paidAt), COALESCE(SUM(p.amount), 0), COUNT(p)
                FROM Payment p
                WHERE p.status = com.hotelbooking.database.PaymentStatus.SUCCESS
                  AND p.paidAt >= :from AND p.paidAt < :to
                GROUP BY FUNCTION('DATE', p.paidAt)
                ORDER BY FUNCTION('DATE', p.paidAt)
                """, Object[].class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
        return mapDateAmount(rows);
    }

    public List<LabeledAmountDto> revenueByMonth(LocalDateTime from, LocalDateTime to) {
        List<Object[]> rows = entityManager.createQuery("""
                SELECT FUNCTION('YEAR', p.paidAt), FUNCTION('MONTH', p.paidAt),
                       COALESCE(SUM(p.amount), 0), COUNT(p)
                FROM Payment p
                WHERE p.status = com.hotelbooking.database.PaymentStatus.SUCCESS
                  AND p.paidAt >= :from AND p.paidAt < :to
                GROUP BY FUNCTION('YEAR', p.paidAt), FUNCTION('MONTH', p.paidAt)
                ORDER BY FUNCTION('YEAR', p.paidAt), FUNCTION('MONTH', p.paidAt)
                """, Object[].class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
        List<LabeledAmountDto> result = new ArrayList<>();
        for (Object[] row : rows) {
            String label = row[0] + "-" + String.format("%02d", ((Number) row[1]).intValue());
            result.add(LabeledAmountDto.builder()
                    .label(label)
                    .amount((BigDecimal) row[2])
                    .count(((Number) row[3]).longValue())
                    .build());
        }
        return result;
    }

    public List<LabeledAmountDto> revenueByRoomType(LocalDateTime from, LocalDateTime to) {
        List<Object[]> rows = entityManager.createQuery("""
                SELECT r.roomType, COALESCE(SUM(br.subtotal), 0), COUNT(DISTINCT b.id)
                FROM BookingRoom br
                JOIN br.booking b
                JOIN br.room r
                JOIN b.payments pay
                WHERE pay.status = com.hotelbooking.database.PaymentStatus.SUCCESS
                  AND pay.paidAt >= :from AND pay.paidAt < :to
                GROUP BY r.roomType
                ORDER BY r.roomType
                """, Object[].class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
        return mapEnumAmount(rows);
    }

    public List<LabeledAmountDto> revenueByBookingStatus(LocalDateTime from, LocalDateTime to) {
        // Native: include CANCELLED rows hidden by Booking @SQLRestriction
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery("""
                SELECT status, COALESCE(SUM(total_amount), 0), COUNT(*)
                FROM bookings
                WHERE created_at >= :from AND created_at < :to
                GROUP BY status
                ORDER BY status
                """)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
        return mapEnumAmount(rows);
    }

    public long countBookingsByStatusBetween(BookingStatus status, LocalDateTime from, LocalDateTime to) {
        Number result = (Number) entityManager.createNativeQuery("""
                SELECT COUNT(*) FROM bookings
                WHERE status = :status AND created_at >= :from AND created_at < :to
                """)
                .setParameter("status", status.name())
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();
        return result.longValue();
    }

    public long countBookingsCreatedBetween(LocalDateTime from, LocalDateTime to) {
        Number result = (Number) entityManager.createNativeQuery("""
                SELECT COUNT(*) FROM bookings
                WHERE created_at >= :from AND created_at < :to
                """)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();
        return result.longValue();
    }

    public long countCheckInsOn(LocalDate date) {
        return entityManager.createQuery("""
                SELECT COUNT(b) FROM Booking b WHERE b.checkInDate = :date
                """, Long.class)
                .setParameter("date", date)
                .getSingleResult();
    }

    public long countCheckOutsOn(LocalDate date) {
        return entityManager.createQuery("""
                SELECT COUNT(b) FROM Booking b WHERE b.checkOutDate = :date
                """, Long.class)
                .setParameter("date", date)
                .getSingleResult();
    }

    public long countGuestsCreatedBetween(LocalDateTime from, LocalDateTime to) {
        return entityManager.createQuery("""
                SELECT COUNT(g) FROM Guest g
                WHERE g.createdAt >= :from AND g.createdAt < :to
                """, Long.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();
    }

    public long countRoomsByStatus(RoomStatus status) {
        return entityManager.createQuery("""
                SELECT COUNT(r) FROM Room r WHERE r.status = :status
                """, Long.class)
                .setParameter("status", status)
                .getSingleResult();
    }

    public long countAllRooms() {
        return entityManager.createQuery("SELECT COUNT(r) FROM Room r", Long.class).getSingleResult();
    }

    public long countAllGuests() {
        return entityManager.createQuery("SELECT COUNT(g) FROM Guest g", Long.class).getSingleResult();
    }

    public long countPaymentsByStatus(PaymentStatus status) {
        return entityManager.createQuery("""
                SELECT COUNT(p) FROM Payment p WHERE p.status = :status
                """, Long.class)
                .setParameter("status", status)
                .getSingleResult();
    }

    public long countPaymentsByStatusBetween(PaymentStatus status, LocalDateTime from, LocalDateTime to) {
        return entityManager.createQuery("""
                SELECT COUNT(p) FROM Payment p
                WHERE p.status = :status AND p.createdAt >= :from AND p.createdAt < :to
                """, Long.class)
                .setParameter("status", status)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();
    }

    public long countBookingsByStatus(BookingStatus status) {
        Number result = (Number) entityManager.createNativeQuery("""
                SELECT COUNT(*) FROM bookings WHERE status = :status
                """)
                .setParameter("status", status.name())
                .getSingleResult();
        return result.longValue();
    }

    public List<LabeledAmountDto> bookingsByStatusBetween(LocalDateTime from, LocalDateTime to) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery("""
                SELECT status, COALESCE(SUM(total_amount), 0), COUNT(*)
                FROM bookings
                WHERE created_at >= :from AND created_at < :to
                GROUP BY status
                ORDER BY status
                """)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
        return mapEnumAmount(rows);
    }

    public List<LabeledAmountDto> bookingsByGuest(LocalDateTime from, LocalDateTime to, int limit) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery("""
                SELECT CONCAT(g.first_name, ' ', g.last_name), COALESCE(SUM(b.total_amount), 0), COUNT(b.id)
                FROM bookings b
                JOIN guests g ON g.id = b.guest_id
                WHERE b.created_at >= :from AND b.created_at < :to
                GROUP BY g.id, g.first_name, g.last_name
                HAVING COUNT(b.id) > 3
                ORDER BY COUNT(b.id) DESC
                """)
                .setParameter("from", from)
                .setParameter("to", to)
                .setMaxResults(limit)
                .getResultList();
        return mapStringAmount(rows);
    }

    public BigDecimal sumBookingTotalsCreatedBetween(LocalDateTime from, LocalDateTime to) {
        Object result = entityManager.createNativeQuery("""
                SELECT COALESCE(SUM(total_amount), 0) FROM bookings
                WHERE created_at >= :from AND created_at < :to
                """)
                .setParameter("from", from)
                .setParameter("to", to)
                .getSingleResult();
        if (result == null) {
            return BigDecimal.ZERO;
        }
        if (result instanceof BigDecimal bd) {
            return bd;
        }
        return BigDecimal.valueOf(((Number) result).doubleValue());
    }

    /**
     * Daily series for a specific payment status (e.g. FAILED-only reports).
     */
    public List<LabeledAmountDto> paymentsByDayForStatus(PaymentStatus status, LocalDateTime from, LocalDateTime to) {
        List<Object[]> rows = entityManager.createQuery("""
                SELECT FUNCTION('DATE', p.createdAt), COALESCE(SUM(p.amount), 0), COUNT(p)
                FROM Payment p
                WHERE p.status = :status
                  AND p.createdAt >= :from AND p.createdAt < :to
                GROUP BY FUNCTION('DATE', p.createdAt)
                ORDER BY FUNCTION('DATE', p.createdAt)
                """, Object[].class)
                .setParameter("status", status)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
        return mapDateAmount(rows);
    }

    public List<LabeledAmountDto> bookingsByRoom(LocalDateTime from, LocalDateTime to, int limit) {
        List<Object[]> rows = entityManager.createQuery("""
                SELECT r.roomNumber, COALESCE(SUM(br.subtotal), 0), COUNT(br)
                FROM BookingRoom br JOIN br.room r JOIN br.booking b
                WHERE b.createdAt >= :from AND b.createdAt < :to
                  AND b.status <> com.hotelbooking.database.BookingStatus.CANCELLED
                GROUP BY r.id, r.roomNumber
                ORDER BY COUNT(br) DESC
                """, Object[].class)
                .setParameter("from", from)
                .setParameter("to", to)
                .setMaxResults(limit)
                .getResultList();
        return mapStringAmount(rows);
    }

    public List<LabeledAmountDto> bookingsByCheckInDate(LocalDate start, LocalDate end) {
        List<Object[]> rows = entityManager.createQuery("""
                SELECT b.checkInDate, COALESCE(SUM(b.totalAmount), 0), COUNT(b)
                FROM Booking b
                WHERE b.checkInDate >= :start AND b.checkInDate <= :end
                GROUP BY b.checkInDate
                ORDER BY b.checkInDate
                """, Object[].class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
        return mapDateAmount(rows);
    }

    public long countUpcomingBookings(LocalDate today) {
        return entityManager.createQuery("""
                SELECT COUNT(b) FROM Booking b
                WHERE b.checkInDate >= :today
                  AND b.status IN (
                    com.hotelbooking.database.BookingStatus.PENDING,
                    com.hotelbooking.database.BookingStatus.CONFIRMED
                  )
                """, Long.class)
                .setParameter("today", today)
                .getSingleResult();
    }

    public long countCompletedBookingsBetween(LocalDateTime from, LocalDateTime to) {
        return countBookingsByStatusBetween(BookingStatus.CHECKED_OUT, from, to);
    }

    /**
     * Room-nights sold in range: sum of nights for booking_rooms whose booking overlaps the range
     * and is not cancelled. Approximation using stored numberOfNights when stay fully inside range;
     * for reporting MVP we count overlapping non-cancelled booking-room rows' nights clipped later in service.
     */
    public List<Object[]> bookingRoomStaysOverlapping(LocalDate start, LocalDate end) {
        return entityManager.createQuery("""
                SELECT b.checkInDate, b.checkOutDate, br.numberOfNights
                FROM BookingRoom br JOIN br.booking b
                WHERE b.status <> com.hotelbooking.database.BookingStatus.CANCELLED
                  AND b.checkInDate < :end
                  AND b.checkOutDate > :start
                """, Object[].class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
    }

    public List<Booking> findRecentBookings(int limit) {
        TypedQuery<Booking> query = entityManager.createQuery("""
                SELECT b FROM Booking b JOIN FETCH b.guest
                ORDER BY b.createdAt DESC
                """, Booking.class);
        query.setMaxResults(limit);
        return query.getResultList();
    }

    private static List<LabeledAmountDto> mapEnumAmount(List<Object[]> rows) {
        List<LabeledAmountDto> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(LabeledAmountDto.builder()
                    .label(String.valueOf(row[0]))
                    .amount(row[1] instanceof BigDecimal bd ? bd : BigDecimal.valueOf(((Number) row[1]).doubleValue()))
                    .count(((Number) row[2]).longValue())
                    .build());
        }
        return result;
    }

    private static List<LabeledAmountDto> mapStringAmount(List<Object[]> rows) {
        return mapEnumAmount(rows);
    }

    private static List<LabeledAmountDto> mapDateAmount(List<Object[]> rows) {
        List<LabeledAmountDto> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(LabeledAmountDto.builder()
                    .label(String.valueOf(row[0]))
                    .amount(row[1] instanceof BigDecimal bd ? bd : BigDecimal.valueOf(((Number) row[1]).doubleValue()))
                    .count(((Number) row[2]).longValue())
                    .build());
        }
        return result;
    }
}
