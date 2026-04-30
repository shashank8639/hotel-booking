package com.hotelbooking.repository;

import com.hotelbooking.database.BookingStatus;
import com.hotelbooking.database.PaymentMethod;
import com.hotelbooking.database.PaymentStatus;
import com.hotelbooking.database.RoomStatus;
import com.hotelbooking.database.RoomType;
import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.BookingRoom;
import com.hotelbooking.entity.Guest;
import com.hotelbooking.entity.Payment;
import com.hotelbooking.entity.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ReportQueryRepository.class)
class ReportQueryRepositoryTest {

    @Autowired
    private ReportQueryRepository reportQueryRepository;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeEach
    void setUp() {
        Guest guest = guestRepository.save(Guest.builder()
                .firstName("Report")
                .lastName("Guest")
                .email("report.guest@example.com")
                .build());

        Room room = roomRepository.save(Room.builder()
                .roomNumber("801")
                .roomType(RoomType.DELUXE)
                .capacity(2)
                .pricePerNight(new BigDecimal("3000"))
                .status(RoomStatus.AVAILABLE)
                .build());

        Booking booking = Booking.builder()
                .guest(guest)
                .checkInDate(LocalDate.now().plusDays(2))
                .checkOutDate(LocalDate.now().plusDays(4))
                .status(BookingStatus.CONFIRMED)
                .totalAmount(new BigDecimal("6000"))
                .build();
        booking.addBookingRoom(BookingRoom.builder()
                .room(room)
                .pricePerNight(new BigDecimal("3000"))
                .numberOfNights(2)
                .subtotal(new BigDecimal("6000"))
                .build());
        booking = bookingRepository.save(booking);

        Payment payment = Payment.builder()
                .booking(booking)
                .amount(new BigDecimal("6000"))
                .refundedAmount(BigDecimal.ZERO)
                .currency("INR")
                .paymentMethod(PaymentMethod.RAZORPAY)
                .status(PaymentStatus.SUCCESS)
                .razorpayOrderId("order_rep_1")
                .razorpayPaymentId("pay_rep_1")
                .transactionReference("pay_rep_1")
                .paidAt(LocalDateTime.now())
                .invoiceNumber("INV-REP-1")
                .build();
        paymentRepository.save(payment);
    }

    @Test
    void countQueries_shouldSeeSeededData() {
        assertThat(reportQueryRepository.countAllGuests()).isGreaterThanOrEqualTo(1);
        assertThat(reportQueryRepository.countAllRooms()).isGreaterThanOrEqualTo(1);
        assertThat(reportQueryRepository.countPaymentsByStatus(PaymentStatus.SUCCESS)).isGreaterThanOrEqualTo(1);
        assertThat(reportQueryRepository.countBookingsByStatus(BookingStatus.CONFIRMED)).isGreaterThanOrEqualTo(1);
    }

    @Test
    void sumSuccessfulPayments_shouldIncludeToday() {
        LocalDateTime from = LocalDate.now().atStartOfDay();
        LocalDateTime to = LocalDate.now().plusDays(1).atStartOfDay();
        assertThat(reportQueryRepository.sumPaymentsByStatusBetween(PaymentStatus.SUCCESS, from, to))
                .isGreaterThanOrEqualTo(new BigDecimal("6000"));
    }
}
