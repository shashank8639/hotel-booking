package com.hotelbooking.service;

import com.hotelbooking.database.BookingStatus;
import com.hotelbooking.dto.AvailabilityResponse;
import com.hotelbooking.dto.BookingRequest;
import com.hotelbooking.dto.BookingResponse;
import com.hotelbooking.dto.BookingStatusRequest;
import com.hotelbooking.dto.RoomAvailabilityMatrixResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    BookingResponse createBooking(BookingRequest request);

    BookingResponse getBookingById(Long id);

    Page<BookingResponse> getAllBookings(Pageable pageable);

    Page<BookingResponse> getBookingsByGuest(
            Long guestId,
            BookingStatus status,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    );

    Page<BookingResponse> getBookingsByStatus(BookingStatus status, Pageable pageable);

    BookingResponse cancelBooking(Long id);

    BookingResponse updateBookingStatus(Long id, BookingStatusRequest request);

    AvailabilityResponse checkAvailability(
            LocalDate checkInDate,
            LocalDate checkOutDate,
            List<Long> roomIds,
            Long excludeBookingId
    );

    /**
     * Day × room availability matrix (default challenge size: 30 days × all active rooms).
     */
    RoomAvailabilityMatrixResponse getAvailabilityMatrix(LocalDate from, Integer days);
}
