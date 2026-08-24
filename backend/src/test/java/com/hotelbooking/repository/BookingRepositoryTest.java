package com.hotelbooking.repository;

import com.hotelbooking.database.BookingStatus;
import com.hotelbooking.database.RoomStatus;
import com.hotelbooking.database.RoomType;
import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.BookingRoom;
import com.hotelbooking.entity.Guest;
import com.hotelbooking.entity.Hotel;
import com.hotelbooking.entity.Room;
import com.hotelbooking.util.HotelTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private StateRepository stateRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private HotelRepository hotelRepository;

    private Guest guest;
    private Room room;

    @BeforeEach
    void setUp() {
        Hotel hotel = HotelTestSupport.persistSampleHotel(
                countryRepository, stateRepository, cityRepository, hotelRepository);

        guest = guestRepository.save(Guest.builder()
                .firstName("Asha")
                .lastName("Patel")
                .email("asha.patel@example.com")
                .phone("+91-9000000001")
                .build());

        room = roomRepository.save(Room.builder()
                .hotel(hotel)
                .roomNumber("501")
                .roomType(RoomType.DELUXE)
                .capacity(2)
                .pricePerNight(new BigDecimal("4000.00"))
                .status(RoomStatus.AVAILABLE)
                .build());
    }

    @Test
    void existsOverlappingBooking_shouldDetectOverlap() {
        saveBooking(LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 15), BookingStatus.CONFIRMED);

        assertThat(bookingRepository.existsOverlappingBooking(
                room.getId(),
                LocalDate.of(2026, 9, 12),
                LocalDate.of(2026, 9, 14)
        )).isTrue();
    }

    @Test
    void existsOverlappingBooking_shouldAllowAdjacentDates() {
        saveBooking(LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 12), BookingStatus.CONFIRMED);

        // Check-out day equals next check-in → no overlap (hotel night model)
        assertThat(bookingRepository.existsOverlappingBooking(
                room.getId(),
                LocalDate.of(2026, 9, 12),
                LocalDate.of(2026, 9, 14)
        )).isFalse();
    }

    @Test
    void existsOverlappingBooking_shouldIgnoreCancelledBookings() {
        saveBooking(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 5), BookingStatus.CANCELLED);

        assertThat(bookingRepository.existsOverlappingBooking(
                room.getId(),
                LocalDate.of(2026, 10, 2),
                LocalDate.of(2026, 10, 4)
        )).isFalse();
    }

    @Test
    void existsOverlappingBooking_shouldRespectExcludeBookingId() {
        Booking existing = saveBooking(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 15), BookingStatus.CONFIRMED);

        assertThat(bookingRepository.existsOverlappingBooking(
                room.getId(),
                LocalDate.of(2026, 11, 12),
                LocalDate.of(2026, 11, 14),
                null
        )).isTrue();

        assertThat(bookingRepository.existsOverlappingBooking(
                room.getId(),
                LocalDate.of(2026, 11, 12),
                LocalDate.of(2026, 11, 14),
                existing.getId()
        )).isFalse();
    }

    @Test
    void findByGuestId_shouldReturnBookings() {
        saveBooking(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3), BookingStatus.PENDING);

        assertThat(bookingRepository.findByGuestId(guest.getId())).hasSize(1);
    }

    @Test
    void findByStatus_shouldHideCancelledViaSqlRestriction() {
        saveBooking(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 5), BookingStatus.CANCELLED);

        assertThat(bookingRepository.findByStatus(BookingStatus.CANCELLED)).isEmpty();
        assertThat(bookingRepository.countAllRowsByGuestId(guest.getId())).isEqualTo(1);
        assertThat(bookingRepository.findCancelledBookings(org.springframework.data.domain.PageRequest.of(0, 10))
                .getTotalElements()).isEqualTo(1);
    }

    @Test
    void findByStatus_shouldFilter() {
        saveBooking(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 2), BookingStatus.PENDING);

        assertThat(bookingRepository.findByStatus(BookingStatus.PENDING)).isNotEmpty();
    }

    @Test
    void findAvailableRoomsForDates_shouldExcludeOverlappingRoom() {
        saveBooking(LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 15), BookingStatus.CONFIRMED);

        assertThat(roomRepository.findAvailableRoomsForDates(
                LocalDate.of(2026, 8, 12),
                LocalDate.of(2026, 8, 14)
        )).extracting(Room::getId).doesNotContain(room.getId());
    }

    private Booking saveBooking(LocalDate checkIn, LocalDate checkOut, BookingStatus status) {
        Booking booking = Booking.builder()
                .guest(guest)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .status(status)
                .totalAmount(new BigDecimal("8000.00"))
                .build();

        int nights = (int) checkIn.until(checkOut).getDays();
        BookingRoom line = BookingRoom.builder()
                .room(room)
                .pricePerNight(room.getPricePerNight())
                .numberOfNights(nights)
                .subtotal(room.getPricePerNight().multiply(BigDecimal.valueOf(nights)))
                .build();
        booking.addBookingRoom(line);
        return bookingRepository.save(booking);
    }
}
