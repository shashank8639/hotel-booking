package com.hotelbooking.repository;

import com.hotelbooking.database.BookingStatus;
import com.hotelbooking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
            SELECT b
            FROM Booking b
            JOIN b.bookingRooms br
            WHERE br.room.id = :roomId
                AND b.status <> 'CANCELLED'
                AND b.checkInDate < :checkOut
                AND b.checkOutDate > :checkIn
            """)
    List<Booking> findOverlappingBookings(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );

    List<Booking> findByGuestId(Long guestId);

    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByGuestIdAndStatus(Long guestId, BookingStatus status);

    List<Booking> findByCheckInDateBetween(LocalDate startDate, LocalDate endDate);

    List<Booking> findByCheckOutDateAfterAndStatus(LocalDate date, BookingStatus status);
}
