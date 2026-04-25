package com.hotelbooking.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelbooking.database.BookingStatus;
import com.hotelbooking.database.RoomType;
import com.hotelbooking.dto.CreatePaymentOrderRequest;
import com.hotelbooking.dto.VerifyPaymentRequest;
import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.Guest;
import com.hotelbooking.entity.Room;
import com.hotelbooking.entity.User;
import com.hotelbooking.payment.MockRazorpayGateway;
import com.hotelbooking.repository.BookingRepository;
import com.hotelbooking.repository.GuestRepository;
import com.hotelbooking.repository.RoleRepository;
import com.hotelbooking.repository.RoomRepository;
import com.hotelbooking.repository.UserRepository;
import com.hotelbooking.security.JwtService;
import com.hotelbooking.security.SecurityConstants;
import com.hotelbooking.security.UserRole;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Payment order create → mock signature verify against H2 + Security.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private MockRazorpayGateway mockRazorpayGateway;

    private final ObjectMapper objectMapper = MockMvcTestSupport.objectMapper();

    private String bearer;
    private Booking booking;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        bookingRepository.deleteAll();
        guestRepository.deleteAll();
        roomRepository.deleteAll();
        IntegrationTestSupport.ensureRoles(roleRepository);

        User customer = IntegrationTestSupport.persistUser(
                userRepository, roleRepository, passwordEncoder,
                "payer@example.com", "password123", UserRole.CUSTOMER
        );
        bearer = IntegrationTestSupport.bearerAccessToken(jwtService, customer);

        Guest guest = guestRepository.save(TestDataFactory.guest("payer.guest@example.com"));
        Room room = roomRepository.save(TestDataFactory.availableRoom(
                "701", RoomType.SUITE, new BigDecimal("9000.00")));

        booking = TestDataFactory.pendingBooking(
                guest,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(7),
                new BigDecimal("18000.00")
        );
        booking.setStatus(BookingStatus.PENDING);
        booking = bookingRepository.save(booking);
        // keep room reference for future assertions / clarity
        assert room.getId() != null;
    }

    @Test
    void createOrder_andVerifyPayment_succeedsWithMockSignature() throws Exception {
        CreatePaymentOrderRequest orderRequest = CreatePaymentOrderRequest.builder()
                .bookingId(booking.getId())
                .build();

        MvcResult orderResult = mockMvc.perform(post("/payments/create-order")
                        .header(SecurityConstants.AUTHORIZATION_HEADER, bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.razorpayOrderId").isNotEmpty())
                .andExpect(jsonPath("$.amount").isNumber())
                .andReturn();

        JsonNode order = objectMapper.readTree(orderResult.getResponse().getContentAsString());
        String orderId = order.get("razorpayOrderId").asText();
        String paymentId = "pay_mock_integration_1";
        String signature = mockRazorpayGateway.signPayment(orderId, paymentId);

        VerifyPaymentRequest verifyRequest = VerifyPaymentRequest.builder()
                .razorpayOrderId(orderId)
                .razorpayPaymentId(paymentId)
                .razorpaySignature(signature)
                .build();

        mockMvc.perform(post("/payments/verify")
                        .header(SecurityConstants.AUTHORIZATION_HEADER, bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        mockMvc.perform(get("/payments/history")
                        .header(SecurityConstants.AUTHORIZATION_HEADER, bearer)
                        .param("bookingId", String.valueOf(booking.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("SUCCESS"));
    }

    @Test
    void createOrder_withoutAuth_isUnauthorized() throws Exception {
        CreatePaymentOrderRequest orderRequest = CreatePaymentOrderRequest.builder()
                .bookingId(booking.getId())
                .build();

        mockMvc.perform(post("/payments/create-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isUnauthorized());
    }
}
