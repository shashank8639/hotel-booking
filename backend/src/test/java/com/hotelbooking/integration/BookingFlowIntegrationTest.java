package com.hotelbooking.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelbooking.database.RoomType;
import com.hotelbooking.dto.BookingRequest;
import com.hotelbooking.dto.GuestRequest;
import com.hotelbooking.entity.Room;
import com.hotelbooking.entity.User;
import com.hotelbooking.repository.CityRepository;
import com.hotelbooking.repository.CountryRepository;
import com.hotelbooking.repository.HotelRepository;
import com.hotelbooking.repository.RoleRepository;
import com.hotelbooking.repository.RoomRepository;
import com.hotelbooking.repository.StateRepository;
import com.hotelbooking.repository.UserRepository;
import com.hotelbooking.security.JwtService;
import com.hotelbooking.security.SecurityConstants;
import com.hotelbooking.security.UserRole;
import com.hotelbooking.util.HotelTestSupport;
import com.hotelbooking.util.IntegrationTestSupport;
import com.hotelbooking.util.MockMvcTestSupport;
import com.hotelbooking.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Critical path: authenticated customer creates guest → books room → cancels.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BookingFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

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

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private final ObjectMapper objectMapper = MockMvcTestSupport.objectMapper();

    private String bearer;
    private Room room;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roomRepository.deleteAll();
        IntegrationTestSupport.ensureRoles(roleRepository);

        User customer = IntegrationTestSupport.persistUser(
                userRepository, roleRepository, passwordEncoder,
                "booker@example.com", "password123", UserRole.CUSTOMER
        );
        bearer = IntegrationTestSupport.bearerAccessToken(jwtService, customer);

        var hotel = HotelTestSupport.persistSampleHotel(
                countryRepository, stateRepository, cityRepository, hotelRepository);
        room = roomRepository.save(TestDataFactory.availableRoom(
                "501", RoomType.DELUXE, new BigDecimal("4500.00"), hotel));
    }

    @Test
    void createGuest_bookRoom_andCancel() throws Exception {
        GuestRequest guestRequest = GuestRequest.builder()
                .firstName("Neha")
                .lastName("Gupta")
                .email("booker@example.com") // must match JWT username for @bookingOwnership.canAccess
                .phone("+91-9111111111")
                .build();

        MvcResult guestResult = mockMvc.perform(post("/guests")
                        .header(SecurityConstants.AUTHORIZATION_HEADER, bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(guestRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        long guestId = objectMapper.readTree(guestResult.getResponse().getContentAsString()).get("id").asLong();

        LocalDate checkIn = LocalDate.now().plusDays(10);
        LocalDate checkOut = checkIn.plusDays(2);

        mockMvc.perform(get("/bookings/availability")
                        .header(SecurityConstants.AUTHORIZATION_HEADER, bearer)
                        .param("roomIds", String.valueOf(room.getId()))
                        .param("checkInDate", checkIn.toString())
                        .param("checkOutDate", checkOut.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rooms[0].available").value(true));

        BookingRequest bookingRequest = BookingRequest.builder()
                .guestId(guestId)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .roomIds(List.of(room.getId()))
                .specialRequests("Late check-in")
                .build();

        MvcResult bookingResult = mockMvc.perform(post("/bookings")
                        .header(SecurityConstants.AUTHORIZATION_HEADER, bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.guestId").value(guestId))
                .andReturn();

        JsonNode booking = objectMapper.readTree(bookingResult.getResponse().getContentAsString());
        long bookingId = booking.get("id").asLong();
        assertThat(booking.get("totalAmount").asDouble()).isGreaterThan(0);

        mockMvc.perform(put("/bookings/" + bookingId + "/cancel")
                        .header(SecurityConstants.AUTHORIZATION_HEADER, bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void overlappingBooking_isRejected() throws Exception {
        GuestRequest guestRequest = GuestRequest.builder()
                .firstName("Overlap")
                .lastName("Guest")
                .email("booker@example.com")
                .phone("+91-9222222222")
                .build();

        long guestId = objectMapper.readTree(
                mockMvc.perform(post("/guests")
                                .header(SecurityConstants.AUTHORIZATION_HEADER, bearer)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(guestRequest)))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString()
        ).get("id").asLong();

        LocalDate checkIn = LocalDate.now().plusDays(20);
        LocalDate checkOut = checkIn.plusDays(3);

        BookingRequest bookingRequest = BookingRequest.builder()
                .guestId(guestId)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .roomIds(List.of(room.getId()))
                .build();

        mockMvc.perform(post("/bookings")
                        .header(SecurityConstants.AUTHORIZATION_HEADER, bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/bookings")
                        .header(SecurityConstants.AUTHORIZATION_HEADER, bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isConflict());
    }
}
