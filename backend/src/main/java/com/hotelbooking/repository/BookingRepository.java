package com.hotelbooking.repository;

import com.hotelbooking.database.BookingStatus;
import com.hotelbooking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByGuestId(Long guestId);

    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByGuestIdAndStatus(Long guestId, BookingStatus status);

    List<Booking> findByCheckInDateBetween(LocalDate startDate, LocalDate endDate);

    List<Booking> findByCheckOutDateAfterAndStatus(LocalDate date, BookingStatus status);
}
