package com.hotelbooking.repository;

import com.hotelbooking.database.RoomStatus;
import com.hotelbooking.database.RoomType;
import com.hotelbooking.entity.Hotel;
import com.hotelbooking.entity.Room;
import com.hotelbooking.util.HotelTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RoomRepositoryTest {

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

    private Hotel hotel;

    @BeforeEach
    void setUp() {
        hotel = HotelTestSupport.persistSampleHotel(
                countryRepository, stateRepository, cityRepository, hotelRepository);

        roomRepository.save(Room.builder()
                .hotel(hotel)
                .roomNumber("101")
                .roomType(RoomType.STANDARD)
                .floorNumber(1)
                .capacity(2)
                .pricePerNight(new BigDecimal("2500.00"))
                .description("Cozy city view standard room")
                .status(RoomStatus.AVAILABLE)
                .build());

        roomRepository.save(Room.builder()
                .hotel(hotel)
                .roomNumber("201")
                .roomType(RoomType.DELUXE)
                .floorNumber(2)
                .capacity(3)
                .pricePerNight(new BigDecimal("4500.00"))
                .status(RoomStatus.MAINTENANCE)
                .build());

        roomRepository.save(Room.builder()
                .hotel(hotel)
                .roomNumber("301")
                .roomType(RoomType.SUITE)
                .floorNumber(3)
                .capacity(4)
                .pricePerNight(new BigDecimal("8500.00"))
                .status(RoomStatus.AVAILABLE)
                .build());
    }

    @Test
    void findByRoomNumber_shouldReturnRoom() {
        assertThat(roomRepository.findByRoomNumber("101")).isPresent();
    }

    @Test
    void findByStatus_shouldFilterAvailableRooms() {
        assertThat(roomRepository.findByStatus(RoomStatus.AVAILABLE)).hasSize(2);
    }

    @Test
    void findByPricePerNightBetween_shouldFilterByRange() {
        Page<Room> page = roomRepository.findByPricePerNightBetween(
                new BigDecimal("2000"),
                new BigDecimal("5000"),
                PageRequest.of(0, 10)
        );
        assertThat(page.getContent()).extracting(Room::getRoomNumber).containsExactlyInAnyOrder("101", "201");
    }

    @Test
    void searchRooms_shouldSupportCombinedFilters() {
        Page<Room> page = roomRepository.searchRooms(
                null,
                RoomType.STANDARD,
                RoomStatus.AVAILABLE,
                1,
                2,
                new BigDecimal("1000"),
                new BigDecimal("3000"),
                "city",
                PageRequest.of(0, 10)
        );
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().getRoomNumber()).isEqualTo("101");
    }

    @Test
    void searchRooms_shouldFilterByFloor() {
        Page<Room> page = roomRepository.searchRooms(
                null, null, null, 2, null, null, null, null, PageRequest.of(0, 10)
        );
        assertThat(page.getContent()).extracting(Room::getRoomNumber).containsExactly("201");
    }

    @Test
    void discountedPriceAboveBase_shouldViolateCheckConstraint() {
        Room invalid = Room.builder()
                .hotel(hotel)
                .roomNumber("999")
                .roomType(RoomType.STANDARD)
                .capacity(2)
                .pricePerNight(new BigDecimal("1000.00"))
                .discountedPrice(new BigDecimal("1500.00"))
                .status(RoomStatus.AVAILABLE)
                .build();

        assertThatThrownBy(() -> roomRepository.saveAndFlush(invalid))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
