package com.hotelbooking.service.impl;

import com.hotelbooking.config.BookingProperties;
import com.hotelbooking.database.BookingStatus;
import com.hotelbooking.database.HotelStatus;
import com.hotelbooking.database.PaymentStatus;
import com.hotelbooking.database.RoomStatus;
import com.hotelbooking.dto.AvailabilityResponse;
import com.hotelbooking.dto.BookingRequest;
import com.hotelbooking.dto.BookingResponse;
import com.hotelbooking.dto.BookingStatusRequest;
import com.hotelbooking.dto.RoomAvailabilityDayItem;
import com.hotelbooking.dto.RoomAvailabilityItem;
import com.hotelbooking.dto.RoomAvailabilityMatrixResponse;
import com.hotelbooking.dto.RoomAvailabilityMatrixRow;
import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.BookingRoom;
import com.hotelbooking.entity.Guest;
import com.hotelbooking.entity.Hotel;
import com.hotelbooking.entity.Payment;
import com.hotelbooking.entity.Room;
import com.hotelbooking.exception.BookingNotFoundException;
import com.hotelbooking.exception.BookingValidationException;
import com.hotelbooking.exception.GuestNotFoundException;
import com.hotelbooking.exception.InvalidBookingStatusTransitionException;
import com.hotelbooking.exception.RoomAlreadyBookedException;
import com.hotelbooking.exception.RoomNotAvailableException;
import com.hotelbooking.exception.RoomNotFoundException;
import com.hotelbooking.mapper.BookingMapper;
import com.hotelbooking.notification.AsyncNotificationFacade;
import com.hotelbooking.repository.BookingRepository;
import com.hotelbooking.repository.GuestRepository;
import com.hotelbooking.repository.PaymentRepository;
import com.hotelbooking.repository.RoomRepository;
import com.hotelbooking.security.BookingOwnership;
import com.hotelbooking.service.BookingService;
import com.hotelbooking.util.BookingDateUtils;
import com.hotelbooking.util.BookingPageables;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private static final int DEFAULT_MATRIX_DAYS = 30;
    private static final int MAX_MATRIX_DAYS = 30;

    private static final Map<BookingStatus, Set<BookingStatus>> ALLOWED_TRANSITIONS = Map.of(
            BookingStatus.PENDING, EnumSet.of(BookingStatus.CONFIRMED, BookingStatus.CANCELLED),
            BookingStatus.CONFIRMED, EnumSet.of(BookingStatus.CHECKED_IN, BookingStatus.CANCELLED),
            BookingStatus.CHECKED_IN, EnumSet.of(BookingStatus.CHECKED_OUT),
            BookingStatus.CHECKED_OUT, EnumSet.noneOf(BookingStatus.class),
            BookingStatus.CANCELLED, EnumSet.noneOf(BookingStatus.class)
    );

    private static final Set<RoomStatus> OPERATIONAL_BLOCKS = EnumSet.of(
            RoomStatus.CLEANING, RoomStatus.MAINTENANCE, RoomStatus.OUT_OF_SERVICE
    );

    private final BookingRepository bookingRepository;
    private final GuestRepository guestRepository;
    private final RoomRepository roomRepository;
    private final PaymentRepository paymentRepository;
    private final BookingMapper bookingMapper;
    private final AsyncNotificationFacade asyncNotificationFacade;
    private final BookingProperties bookingProperties;
    private final BookingOwnership bookingOwnership;

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        log.info("Creating booking for guestId={}, checkIn={}, checkOut={}, rooms={}",
                request.getGuestId(), request.getCheckInDate(), request.getCheckOutDate(), request.getRoomIds());

        int nights = BookingDateUtils.validateAndCalculateNights(request.getCheckInDate(), request.getCheckOutDate());
        List<Long> roomIds = normalizeUniqueRoomIds(request.getRoomIds());

        Guest guest = guestRepository.findById(request.getGuestId())
                .orElseThrow(() -> new GuestNotFoundException(request.getGuestId()));
        bookingOwnership.assertGuestEmailAllowed(guest.getEmail());

        Booking booking = Booking.builder()
                .guest(guest)
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .status(BookingStatus.PENDING)
                .specialRequests(request.getSpecialRequests())
                .holdExpiresAt(LocalDateTime.now().plusMinutes(bookingProperties.getPendingHoldMinutes()))
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        List<Long> lockedRoomIds = new ArrayList<>();
        Long hotelId = null;

        for (Long roomId : roomIds) {
            try {
                Room room = roomRepository.findByIdForUpdate(roomId)
                        .orElseThrow(() -> new RoomNotFoundException(roomId));

                Long roomHotelId = room.getHotel() != null ? room.getHotel().getId() : null;
                if (roomHotelId == null) {
                    throw new BookingValidationException("Room " + room.getRoomNumber() + " is not linked to a hotel");
                }
                if (hotelId == null) {
                    hotelId = roomHotelId;
                } else if (!Objects.equals(hotelId, roomHotelId)) {
                    throw new BookingValidationException("All rooms in a booking must belong to the same hotel");
                }

                assertHotelBookable(room.getHotel());
                assertRoomOperationallyBookable(room);
                assertNoDateOverlap(room, request.getCheckInDate(), request.getCheckOutDate(), null);

                BigDecimal nightlyRate = room.getEffectivePrice();
                BigDecimal subtotal = nightlyRate.multiply(BigDecimal.valueOf(nights));

                BookingRoom line = BookingRoom.builder()
                        .room(room)
                        .pricePerNight(nightlyRate)
                        .numberOfNights(nights)
                        .subtotal(subtotal)
                        .build();

                booking.addBookingRoom(line);
                total = total.add(subtotal);
                lockedRoomIds.add(roomId);
            } catch (RuntimeException ex) {
                // Transaction will roll back; log which rooms were already locked before failure
                log.warn(
                        "Multi-room booking partial failure: guestId={}, failedRoomId={}, alreadyLockedRoomIds={}, checkIn={}, checkOut={}, cause={}",
                        request.getGuestId(),
                        roomId,
                        lockedRoomIds,
                        request.getCheckInDate(),
                        request.getCheckOutDate(),
                        ex.toString()
                );
                throw ex;
            }
        }

        booking.setTotalAmount(total);
        Booking saved = bookingRepository.save(booking);

        log.info("Booking created id={}, totalAmount={}, nights={}, holdExpiresAt={}",
                saved.getId(), saved.getTotalAmount(), nights, saved.getHoldExpiresAt());
        return bookingMapper.toResponse(saved);
    }

    @Override
    public BookingResponse getBookingById(Long id) {
        log.debug("Fetching booking id={}", id);
        return bookingMapper.toResponse(findBookingOrThrow(id));
    }

    @Override
    public Page<BookingResponse> getAllBookings(Pageable pageable) {
        Pageable constrained = BookingPageables.constrain(pageable);
        log.debug("Fetching bookings page={}, size={}", constrained.getPageNumber(), constrained.getPageSize());
        return bookingRepository.findAll(constrained).map(bookingMapper::toResponse);
    }

    @Override
    public Page<BookingResponse> getBookingsByGuest(
            Long guestId,
            BookingStatus status,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    ) {
        if (!guestRepository.existsById(guestId)) {
            throw new GuestNotFoundException(guestId);
        }
        Pageable constrained = BookingPageables.constrain(pageable);
        log.debug("Fetching my-bookings guestId={}, status={}, from={}, to={}", guestId, status, from, to);
        return bookingRepository.findMyBookings(guestId, status, from, to, constrained)
                .map(bookingMapper::toResponse);
    }

    @Override
    public Page<BookingResponse> getBookingsByStatus(BookingStatus status, Pageable pageable) {
        Pageable constrained = BookingPageables.constrain(pageable);
        log.debug("Fetching bookings with status={}", status);
        if (status == BookingStatus.CANCELLED) {
            return bookingRepository.findCancelledBookings(constrained).map(bookingMapper::toResponse);
        }
        return bookingRepository.findByStatus(status, constrained).map(bookingMapper::toResponse);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(Long id) {
        log.info("Cancelling booking id={}", id);
        Booking booking = findBookingOrThrow(id);
        validateStatusTransition(booking.getStatus(), BookingStatus.CANCELLED);

        boolean hasOpenSuccessPayment = paymentRepository.existsByBookingIdAndStatus(id, PaymentStatus.SUCCESS);
        if (hasOpenSuccessPayment) {
            throw new BookingValidationException(
                    "Cannot cancel a paid booking until the payment is fully refunded (ADMIN refund API)"
            );
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setHoldExpiresAt(null);
        Booking saved = bookingRepository.save(booking);
        log.info("Booking cancelled id={}", id);

        BigDecimal refunded = paymentRepository.findByBookingId(id).stream()
                .map(Payment::getRefundedAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        boolean hasRefundedPayment = paymentRepository.existsByBookingIdAndStatus(id, PaymentStatus.REFUNDED);
        String refundStatus = hasRefundedPayment
                ? (refunded.compareTo(BigDecimal.ZERO) > 0 ? "Refund initiated / processed" : "Refunded")
                : "No payment collected";

        asyncNotificationFacade.bookingCancellationAsync(saved.getId(), refundStatus, refunded);
        return bookingMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BookingResponse updateBookingStatus(Long id, BookingStatusRequest request) {
        log.info("Updating booking id={} status to {}", id, request.getStatus());
        Booking booking = findBookingOrThrow(id);

        if (request.getStatus() == booking.getStatus()) {
            return bookingMapper.toResponse(booking);
        }

        validateStatusTransition(booking.getStatus(), request.getStatus());
        booking.setStatus(request.getStatus());
        if (request.getStatus() != BookingStatus.PENDING) {
            booking.setHoldExpiresAt(null);
        }
        return bookingMapper.toResponse(bookingRepository.save(booking));
    }

    @Override
    public AvailabilityResponse checkAvailability(
            LocalDate checkInDate,
            LocalDate checkOutDate,
            List<Long> roomIds,
            Long excludeBookingId
    ) {
        log.debug("Checking availability checkIn={}, checkOut={}, roomIds={}, excludeBookingId={}",
                checkInDate, checkOutDate, roomIds, excludeBookingId);
        int nights = BookingDateUtils.validateAndCalculateNights(checkInDate, checkOutDate);

        List<Room> roomsToCheck;
        if (roomIds == null || roomIds.isEmpty()) {
            roomsToCheck = roomRepository.findByDeletedFalse(
                    org.springframework.data.domain.Pageable.unpaged()
            ).getContent();
        } else {
            List<Long> uniqueIds = normalizeUniqueRoomIds(roomIds);
            roomsToCheck = uniqueIds.stream()
                    .map(id -> roomRepository.findByIdAndDeletedFalse(id)
                            .orElseThrow(() -> new RoomNotFoundException(id)))
                    .toList();
        }

        List<RoomAvailabilityItem> items = roomsToCheck.stream()
                .map(room -> toAvailabilityItem(room, checkInDate, checkOutDate, excludeBookingId))
                .toList();

        return AvailabilityResponse.builder()
                .checkInDate(checkInDate)
                .checkOutDate(checkOutDate)
                .numberOfNights(nights)
                .rooms(items)
                .build();
    }

    @Override
    public RoomAvailabilityMatrixResponse getAvailabilityMatrix(LocalDate from, Integer days) {
        LocalDate start = from != null ? from : LocalDate.now();
        int dayCount = days == null ? DEFAULT_MATRIX_DAYS : days;
        if (dayCount < 1 || dayCount > MAX_MATRIX_DAYS) {
            throw new BookingValidationException("days must be between 1 and " + MAX_MATRIX_DAYS);
        }
        LocalDate end = start.plusDays(dayCount);

        List<LocalDate> dates = start.datesUntil(end).toList();
        List<Room> rooms = roomRepository.findByDeletedFalse(
                org.springframework.data.domain.PageRequest.of(0, 500)
        ).getContent();

        List<RoomAvailabilityMatrixRow> rows = new ArrayList<>();
        for (Room room : rooms) {
            List<Booking> overlaps = bookingRepository.findOverlappingBookings(
                    room.getId(), start, end, null
            );
            Set<LocalDate> bookedNights = new HashSet<>();
            for (Booking booking : overlaps) {
                LocalDate overlapStart = booking.getCheckInDate().isBefore(start) ? start : booking.getCheckInDate();
                LocalDate overlapEnd = booking.getCheckOutDate().isAfter(end) ? end : booking.getCheckOutDate();
                overlapStart.datesUntil(overlapEnd).forEach(bookedNights::add);
            }

            String operationalBlock = OPERATIONAL_BLOCKS.contains(room.getStatus())
                    ? room.getStatus().name()
                    : null;

            List<RoomAvailabilityDayItem> dayItems = new ArrayList<>();
            for (LocalDate date : dates) {
                if (operationalBlock != null) {
                    dayItems.add(RoomAvailabilityDayItem.builder()
                            .date(date).available(false).blockReason(operationalBlock).build());
                } else if (room.getStatus() != RoomStatus.AVAILABLE && room.getStatus() != RoomStatus.RESERVED) {
                    dayItems.add(RoomAvailabilityDayItem.builder()
                            .date(date).available(false).blockReason(room.getStatus().name()).build());
                } else if (bookedNights.contains(date)) {
                    dayItems.add(RoomAvailabilityDayItem.builder()
                            .date(date).available(false).blockReason("BOOKED").build());
                } else {
                    dayItems.add(RoomAvailabilityDayItem.builder()
                            .date(date).available(true).blockReason(null).build());
                }
            }

            rows.add(RoomAvailabilityMatrixRow.builder()
                    .roomId(room.getId())
                    .roomNumber(room.getRoomNumber())
                    .roomType(room.getRoomType())
                    .roomStatus(room.getStatus())
                    .days(dayItems)
                    .build());
        }

        return RoomAvailabilityMatrixResponse.builder()
                .from(start)
                .to(end)
                .dayCount(dayCount)
                .dates(dates)
                .rooms(rows)
                .build();
    }

    private RoomAvailabilityItem toAvailabilityItem(
            Room room,
            LocalDate checkIn,
            LocalDate checkOut,
            Long excludeBookingId
    ) {
        if (room.getStatus() != RoomStatus.AVAILABLE) {
            return RoomAvailabilityItem.builder()
                    .roomId(room.getId())
                    .roomNumber(room.getRoomNumber())
                    .roomType(room.getRoomType())
                    .roomStatus(room.getStatus())
                    .effectivePrice(room.getEffectivePrice())
                    .available(false)
                    .reason("Room status is " + room.getStatus())
                    .build();
        }

        boolean overlap = bookingRepository.existsOverlappingBooking(
                room.getId(), checkIn, checkOut, excludeBookingId
        );
        return RoomAvailabilityItem.builder()
                .roomId(room.getId())
                .roomNumber(room.getRoomNumber())
                .roomType(room.getRoomType())
                .roomStatus(room.getStatus())
                .effectivePrice(room.getEffectivePrice())
                .available(!overlap)
                .reason(overlap ? "Overlapping booking exists for the selected dates" : null)
                .build();
    }

    private List<Long> normalizeUniqueRoomIds(List<Long> roomIds) {
        if (roomIds == null || roomIds.isEmpty()) {
            throw new BookingValidationException("At least one room ID is required");
        }
        if (roomIds.stream().anyMatch(id -> id == null)) {
            throw new BookingValidationException("Room IDs must not contain null values");
        }
        Set<Long> unique = new HashSet<>();
        for (Long id : roomIds) {
            if (!unique.add(id)) {
                throw new BookingValidationException("Duplicate room ID in request: " + id);
            }
        }
        return List.copyOf(unique);
    }

    private void assertHotelBookable(Hotel hotel) {
        if (hotel == null) {
            throw new BookingValidationException("Room is not linked to a hotel");
        }
        if (hotel.getStatus() != HotelStatus.APPROVED || !hotel.isVerified()) {
            throw new BookingValidationException(
                    "Hotel '" + hotel.getName() + "' is not available for booking"
            );
        }
    }

    private void assertRoomOperationallyBookable(Room room) {
        if (room.getStatus() != RoomStatus.AVAILABLE) {
            // Explicitly covers OUT_OF_SERVICE, CLEANING, MAINTENANCE, OCCUPIED, RESERVED
            log.warn("Blocking booking for roomId={} roomNumber={} status={}",
                    room.getId(), room.getRoomNumber(), room.getStatus());
            throw new RoomNotAvailableException(room.getId(), room.getRoomNumber(), room.getStatus().name());
        }
    }

    private void assertNoDateOverlap(Room room, LocalDate checkIn, LocalDate checkOut, Long excludeBookingId) {
        if (bookingRepository.existsOverlappingBooking(room.getId(), checkIn, checkOut, excludeBookingId)) {
            log.warn("Overlap detected for roomId={} between {} and {} (excludeBookingId={})",
                    room.getId(), checkIn, checkOut, excludeBookingId);
            throw new RoomAlreadyBookedException(room.getId(), room.getRoomNumber());
        }
    }

    private void validateStatusTransition(BookingStatus current, BookingStatus next) {
        Set<BookingStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(current, EnumSet.noneOf(BookingStatus.class));
        if (!allowed.contains(next)) {
            throw new InvalidBookingStatusTransitionException(
                    "Cannot transition booking status from " + current + " to " + next
            );
        }
    }

    private Booking findBookingOrThrow(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));
    }
}
