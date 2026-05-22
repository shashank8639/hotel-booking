package com.hotelbooking.repository;

import com.hotelbooking.entity.BookingRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRoomRepository extends JpaRepository<BookingRoom, Long> {

    List<BookingRoom> findByBookingId(Long bookingId);

    List<BookingRoom> findByRoomId(Long roomId);

    boolean existsByBookingIdAndRoomId(Long bookingId, Long roomId);
}
