package com.hotelbooking.repository;

import com.hotelbooking.database.BookingStatus;
import com.hotelbooking.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Classic date-range overlap for a single room.
     * Pass {@code excludeBookingId} when re-checking dates for an existing booking (update API).
     */
    @Query("""
            SELECT b
            FROM Booking b
            JOIN b.bookingRooms br
            WHERE br.room.id = :roomId
              AND b.status <> com.hotelbooking.database.BookingStatus.CANCELLED
              AND (:excludeBookingId IS NULL OR b.id <> :excludeBookingId)
              AND b.checkInDate < :checkOut
              AND b.checkOutDate > :checkIn
            """)
    List<Booking> findOverlappingBookings(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("excludeBookingId") Long excludeBookingId
    );

    default List<Booking> findOverlappingBookings(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        return findOverlappingBookings(roomId, checkIn, checkOut, null);
    }

    @Query("""
            SELECT CASE WHEN COUNT(b) > 0 THEN TRUE ELSE FALSE END
            FROM Booking b
            JOIN b.bookingRooms br
            WHERE br.room.id = :roomId
              AND b.status <> com.hotelbooking.database.BookingStatus.CANCELLED
              AND (:excludeBookingId IS NULL OR b.id <> :excludeBookingId)
              AND b.checkInDate < :checkOut
              AND b.checkOutDate > :checkIn
            """)
    boolean existsOverlappingBooking(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("excludeBookingId") Long excludeBookingId
    );

    default boolean existsOverlappingBooking(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        return existsOverlappingBooking(roomId, checkIn, checkOut, null);
    }

    List<Booking> findByGuestId(Long guestId);

    Page<Booking> findByGuestId(Long guestId, Pageable pageable);

    List<Booking> findByStatus(BookingStatus status);

    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

    /**
     * Bypasses {@code @SQLRestriction} so admin history can list cancelled bookings.
     */
    @Query(
            value = "SELECT * FROM bookings WHERE status = 'CANCELLED'",
            countQuery = "SELECT count(*) FROM bookings WHERE status = 'CANCELLED'",
            nativeQuery = true
    )
    Page<Booking> findCancelledBookings(Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM bookings WHERE guest_id = :guestId", nativeQuery = true)
    long countAllRowsByGuestId(@Param("guestId") Long guestId);

    List<Booking> findByGuestIdAndStatus(Long guestId, BookingStatus status);

    Page<Booking> findByGuestIdAndStatus(Long guestId, BookingStatus status, Pageable pageable);

    /**
     * Guest "my bookings" filter: optional status + check-in window.
     */
    @Query("""
            SELECT b FROM Booking b
            WHERE b.guest.id = :guestId
              AND (:status IS NULL OR b.status = :status)
              AND (:from IS NULL OR b.checkInDate >= :from)
              AND (:to IS NULL OR b.checkInDate <= :to)
            """)
    Page<Booking> findMyBookings(
            @Param("guestId") Long guestId,
            @Param("status") BookingStatus status,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            Pageable pageable
    );

    List<Booking> findByCheckInDateBetween(LocalDate startDate, LocalDate endDate);

    Page<Booking> findByCheckInDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    List<Booking> findByCheckOutDateAfterAndStatus(LocalDate date, BookingStatus status);

    @Query("""
            SELECT b FROM Booking b
            WHERE b.status IN (
                com.hotelbooking.database.BookingStatus.PENDING,
                com.hotelbooking.database.BookingStatus.CONFIRMED,
                com.hotelbooking.database.BookingStatus.CHECKED_IN
            )
            """)
    List<Booking> findActiveBookings();

    @Query("""
            SELECT b FROM Booking b
            WHERE b.checkInDate <= :endDate
              AND b.checkOutDate >= :startDate
              AND b.status <> com.hotelbooking.database.BookingStatus.CANCELLED
            """)
    Page<Booking> findBookingsOverlappingDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query("""
            SELECT b FROM Booking b
            WHERE b.status = com.hotelbooking.database.BookingStatus.PENDING
              AND b.holdExpiresAt IS NOT NULL
              AND b.holdExpiresAt < :now
            """)
    List<Booking> findExpiredPendingHolds(@Param("now") LocalDateTime now);

    /**
     * Bulk cancel expired PENDING holds (native so soft-hide filters never interfere).
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE bookings
            SET status = 'CANCELLED', updated_at = CURRENT_TIMESTAMP
            WHERE status = 'PENDING'
              AND hold_expires_at IS NOT NULL
              AND hold_expires_at < :now
            """, nativeQuery = true)
    int cancelExpiredPendingHolds(@Param("now") LocalDateTime now);
}
