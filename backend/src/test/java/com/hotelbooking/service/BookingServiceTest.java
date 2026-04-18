package com.hotelbooking.service;

import com.hotelbooking.config.BookingProperties;
import com.hotelbooking.database.BookingStatus;
import com.hotelbooking.database.HotelStatus;
import com.hotelbooking.database.RoomStatus;
import com.hotelbooking.database.RoomType;
import com.hotelbooking.dto.AvailabilityResponse;
import com.hotelbooking.dto.BookingRequest;
import com.hotelbooking.dto.BookingResponse;
import com.hotelbooking.dto.BookingStatusRequest;
import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.Guest;
import com.hotelbooking.entity.Hotel;
import com.hotelbooking.entity.Room;
import com.hotelbooking.exception.BookingNotFoundException;
import com.hotelbooking.exception.BookingValidationException;
import com.hotelbooking.exception.GuestNotFoundException;
import com.hotelbooking.exception.InvalidBookingDatesException;
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
import com.hotelbooking.service.impl.BookingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private GuestRepository guestRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private AsyncNotificationFacade asyncNotificationFacade;

    @Mock
    private BookingProperties bookingProperties;

    @Mock
    private BookingOwnership bookingOwnership;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private Guest guest;
    private Room room;
    private LocalDate checkIn;
    private LocalDate checkOut;

    @BeforeEach
    void setUp() {
        lenient().when(bookingProperties.getPendingHoldMinutes()).thenReturn(15);
        lenient().doNothing().when(bookingOwnership).assertGuestEmailAllowed(anyString());

        guest = Guest.builder()
                .firstName("Rahul")
                .lastName("Sharma")
                .email("rahul@example.com")
                .build();
        guest.setId(1L);

        Hotel hotel = Hotel.builder()
                .name("Test Hotel")
                .slug("test-hotel")
                .status(HotelStatus.APPROVED)
                .verified(true)
                .build();
        hotel.setId(100L);

        room = Room.builder()
                .hotel(hotel)
                .roomNumber("101")
                .roomType(RoomType.STANDARD)
                .capacity(2)
                .pricePerNight(new BigDecimal("2500.00"))
                .status(RoomStatus.AVAILABLE)
                .build();
        room.setId(10L);

        checkIn = LocalDate.now().plusDays(7);
        checkOut = checkIn.plusDays(2);
    }

    @Test
    void createBooking_shouldCalculateTotalAndSnapshotPrice() {
        BookingRequest request = BookingRequest.builder()
                .guestId(1L)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .roomIds(List.of(10L))
                .build();

        when(guestRepository.findById(1L)).thenReturn(Optional.of(guest));
        when(roomRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(room));
        when(bookingRepository.existsOverlappingBooking(10L, checkIn, checkOut, null)).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(100L);
            return b;
        });
        when(bookingMapper.toResponse(any(Booking.class))).thenReturn(
                BookingResponse.builder().id(100L).totalAmount(new BigDecimal("5000.00")).build()
        );

        BookingResponse response = bookingService.createBooking(request);

        assertThat(response.getId()).isEqualTo(100L);

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());
        Booking saved = captor.getValue();
        assertThat(saved.getTotalAmount()).isEqualByComparingTo("5000.00");
        assertThat(saved.getBookingRooms()).hasSize(1);
        assertThat(saved.getBookingRooms().get(0).getPricePerNight()).isEqualByComparingTo("2500.00");
        assertThat(saved.getBookingRooms().get(0).getNumberOfNights()).isEqualTo(2);
    }

    @Test
    void createBooking_shouldRejectPastCheckIn() {
        BookingRequest request = BookingRequest.builder()
                .guestId(1L)
                .checkInDate(LocalDate.now().minusDays(1))
                .checkOutDate(LocalDate.now().plusDays(1))
                .roomIds(List.of(10L))
                .build();

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(InvalidBookingDatesException.class);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_shouldRejectDuplicateRoomIds() {
        BookingRequest request = BookingRequest.builder()
                .guestId(1L)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .roomIds(List.of(10L, 10L))
                .build();

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(BookingValidationException.class);
    }

    @Test
    void createBooking_shouldRejectMissingGuest() {
        BookingRequest request = BookingRequest.builder()
                .guestId(99L)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .roomIds(List.of(10L))
                .build();
        when(guestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(GuestNotFoundException.class);
    }

    @Test
    void createBooking_shouldRejectMissingRoom() {
        BookingRequest request = BookingRequest.builder()
                .guestId(1L)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .roomIds(List.of(10L))
                .build();
        when(guestRepository.findById(1L)).thenReturn(Optional.of(guest));
        when(roomRepository.findByIdForUpdate(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(RoomNotFoundException.class);
    }

    @Test
    void createBooking_shouldRejectMaintenanceRoom() {
        room.setStatus(RoomStatus.MAINTENANCE);
        BookingRequest request = BookingRequest.builder()
                .guestId(1L)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .roomIds(List.of(10L))
                .build();
        when(guestRepository.findById(1L)).thenReturn(Optional.of(guest));
        when(roomRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(RoomNotAvailableException.class);
    }

    @Test
    void createBooking_shouldRejectOutOfServiceRoom() {
        room.setStatus(RoomStatus.OUT_OF_SERVICE);
        BookingRequest request = BookingRequest.builder()
                .guestId(1L)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .roomIds(List.of(10L))
                .build();
        when(guestRepository.findById(1L)).thenReturn(Optional.of(guest));
        when(roomRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(RoomNotAvailableException.class);
    }

    @Test
    void createBooking_shouldSetSoftHoldExpiry() {
        BookingRequest request = BookingRequest.builder()
                .guestId(1L)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .roomIds(List.of(10L))
                .build();
        when(guestRepository.findById(1L)).thenReturn(Optional.of(guest));
        when(roomRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(room));
        when(bookingRepository.existsOverlappingBooking(10L, checkIn, checkOut, null)).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookingMapper.toResponse(any())).thenReturn(BookingResponse.builder().build());

        bookingService.createBooking(request);

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());
        assertThat(captor.getValue().getHoldExpiresAt()).isNotNull();
    }

    /**
     * Mockito practice #3 — stub existsOverlappingBooking(true) to force RoomAlreadyBookedException.
     */
    @Test
    void createBooking_shouldRejectOverlappingBooking() {
        BookingRequest request = BookingRequest.builder()
                .guestId(1L)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .roomIds(List.of(10L))
                .build();
        when(guestRepository.findById(1L)).thenReturn(Optional.of(guest));
        when(roomRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(room));
        when(bookingRepository.existsOverlappingBooking(10L, checkIn, checkOut, null)).thenReturn(true);

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(RoomAlreadyBookedException.class);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_shouldUseDiscountedPriceAsSnapshot() {
        room.setDiscountedPrice(new BigDecimal("2000.00"));
        BookingRequest request = BookingRequest.builder()
                .guestId(1L)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .roomIds(List.of(10L))
                .build();
        when(guestRepository.findById(1L)).thenReturn(Optional.of(guest));
        when(roomRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(room));
        when(bookingRepository.existsOverlappingBooking(10L, checkIn, checkOut, null)).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookingMapper.toResponse(any())).thenReturn(BookingResponse.builder().build());

        bookingService.createBooking(request);

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());
        assertThat(captor.getValue().getTotalAmount()).isEqualByComparingTo("4000.00");
        assertThat(captor.getValue().getBookingRooms().get(0).getPricePerNight())
                .isEqualByComparingTo("2000.00");
    }

    @Test
    void cancelBooking_shouldAllowPending() {
        Booking booking = Booking.builder()
                .guest(guest)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .status(BookingStatus.PENDING)
                .totalAmount(BigDecimal.TEN)
                .build();
        booking.setId(5L);

        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(paymentRepository.findByBookingId(5L)).thenReturn(List.of());
        when(paymentRepository.existsByBookingIdAndStatus(eq(5L), any())).thenReturn(false);
        when(bookingMapper.toResponse(booking)).thenReturn(
                BookingResponse.builder().id(5L).status(BookingStatus.CANCELLED).build()
        );

        BookingResponse response = bookingService.cancelBooking(5L);

        assertThat(response.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(asyncNotificationFacade).bookingCancellationAsync(eq(5L), any(), any());
    }

    @Test
    void cancelBooking_shouldRejectCheckedIn() {
        Booking booking = Booking.builder()
                .guest(guest)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .status(BookingStatus.CHECKED_IN)
                .totalAmount(BigDecimal.TEN)
                .build();
        booking.setId(5L);
        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking(5L))
                .isInstanceOf(InvalidBookingStatusTransitionException.class);
    }

    @Test
    void updateBookingStatus_shouldConfirmPending() {
        Booking booking = Booking.builder()
                .guest(guest)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .status(BookingStatus.PENDING)
                .totalAmount(BigDecimal.TEN)
                .build();
        booking.setId(5L);
        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingMapper.toResponse(booking)).thenReturn(
                BookingResponse.builder().id(5L).status(BookingStatus.CONFIRMED).build()
        );

        BookingResponse response = bookingService.updateBookingStatus(
                5L,
                BookingStatusRequest.builder().status(BookingStatus.CONFIRMED).build()
        );

        assertThat(response.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void getBookingById_shouldThrowWhenMissing() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingById(99L))
                .isInstanceOf(BookingNotFoundException.class);
    }

    @Test
    void checkAvailability_shouldMarkOverlappingRoomUnavailable() {
        when(roomRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(room));
        when(bookingRepository.existsOverlappingBooking(eq(10L), eq(checkIn), eq(checkOut), isNull())).thenReturn(true);

        AvailabilityResponse response = bookingService.checkAvailability(checkIn, checkOut, List.of(10L), null);

        assertThat(response.getNumberOfNights()).isEqualTo(2);
        assertThat(response.getRooms()).hasSize(1);
        assertThat(response.getRooms().get(0).isAvailable()).isFalse();
    }

    @Test
    void checkAvailability_shouldIgnoreSelfWhenExcludeBookingIdProvided() {
        when(roomRepository.findByIdAndDeletedFalse(10L)).thenReturn(Optional.of(room));
        when(bookingRepository.existsOverlappingBooking(eq(10L), eq(checkIn), eq(checkOut), eq(55L))).thenReturn(false);

        AvailabilityResponse response = bookingService.checkAvailability(checkIn, checkOut, List.of(10L), 55L);

        assertThat(response.getRooms().get(0).isAvailable()).isTrue();
        verify(bookingRepository).existsOverlappingBooking(10L, checkIn, checkOut, 55L);
    }

    @Test
    void getBookingsByGuest_shouldRequireExistingGuest() {
        when(guestRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> bookingService.getBookingsByGuest(99L, null, null, null, PageRequest.of(0, 10)))
                .isInstanceOf(GuestNotFoundException.class);
    }

    @Test
    void getAllBookings_shouldMapPage() {
        Booking booking = Booking.builder()
                .guest(guest)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .status(BookingStatus.PENDING)
                .totalAmount(BigDecimal.TEN)
                .build();
        when(bookingRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(booking), PageRequest.of(0, 10), 1));
        when(bookingMapper.toResponse(booking)).thenReturn(BookingResponse.builder().id(1L).build());

        assertThat(bookingService.getAllBookings(PageRequest.of(0, 10)).getContent()).hasSize(1);
    }
}
