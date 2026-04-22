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
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private RoomRepository roomRepository;

    private Booking booking;

    @BeforeEach
    void setUp() {
        Guest guest = guestRepository.save(Guest.builder()
                .firstName("Pay")
                .lastName("Guest")
                .email("pay.guest@example.com")
                .build());

        Room room = roomRepository.save(Room.builder()
                .roomNumber("901")
                .roomType(RoomType.STANDARD)
                .capacity(2)
                .pricePerNight(new BigDecimal("2000.00"))
                .status(RoomStatus.AVAILABLE)
                .build());

        booking = Booking.builder()
                .guest(guest)
                .checkInDate(LocalDate.now().plusDays(20))
                .checkOutDate(LocalDate.now().plusDays(22))
                .status(BookingStatus.PENDING)
                .totalAmount(new BigDecimal("4000.00"))
                .build();
        BookingRoom line = BookingRoom.builder()
                .room(room)
                .pricePerNight(new BigDecimal("2000.00"))
                .numberOfNights(2)
                .subtotal(new BigDecimal("4000.00"))
                .build();
        booking.addBookingRoom(line);
        booking = bookingRepository.save(booking);
    }

    @Test
    void findByRazorpayOrderId_shouldReturnPayment() {
        paymentRepository.save(Payment.builder()
                .booking(booking)
                .amount(new BigDecimal("4000.00"))
                .refundedAmount(BigDecimal.ZERO)
                .currency("INR")
                .paymentMethod(PaymentMethod.RAZORPAY)
                .status(PaymentStatus.PENDING)
                .razorpayOrderId("order_repo_1")
                .transactionReference("order_repo_1")
                .build());

        assertThat(paymentRepository.findByRazorpayOrderId("order_repo_1")).isPresent();
    }

    @Test
    void existsByBookingIdAndStatus_shouldDetectSuccess() {
        paymentRepository.save(Payment.builder()
                .booking(booking)
                .amount(new BigDecimal("4000.00"))
                .refundedAmount(BigDecimal.ZERO)
                .currency("INR")
                .paymentMethod(PaymentMethod.RAZORPAY)
                .status(PaymentStatus.SUCCESS)
                .razorpayOrderId("order_repo_2")
                .razorpayPaymentId("pay_repo_2")
                .transactionReference("pay_repo_2")
                .paidAt(LocalDateTime.now())
                .invoiceNumber("INV-TEST-0001")
                .build());

        assertThat(paymentRepository.existsByBookingIdAndStatus(booking.getId(), PaymentStatus.SUCCESS)).isTrue();
    }

    @Test
    void findByGuestId_shouldPageResults() {
        paymentRepository.save(Payment.builder()
                .booking(booking)
                .amount(new BigDecimal("4000.00"))
                .refundedAmount(BigDecimal.ZERO)
                .currency("INR")
                .paymentMethod(PaymentMethod.RAZORPAY)
                .status(PaymentStatus.PENDING)
                .razorpayOrderId("order_repo_3")
                .transactionReference("order_repo_3")
                .build());

        assertThat(paymentRepository.findByGuestId(booking.getGuest().getId(), PageRequest.of(0, 10))
                .getContent()).isNotEmpty();
    }

    /**
     * Practice #2 — payment history filters: by bookingId, then by status.
     */
    @Test
    void findByBookingId_andStatus_filtersPaymentHistory() {
        paymentRepository.save(Payment.builder()
                .booking(booking)
                .amount(new BigDecimal("4000.00"))
                .refundedAmount(BigDecimal.ZERO)
                .currency("INR")
                .paymentMethod(PaymentMethod.RAZORPAY)
                .status(PaymentStatus.PENDING)
                .razorpayOrderId("order_hist_pending")
                .transactionReference("order_hist_pending")
                .build());

        paymentRepository.save(Payment.builder()
                .booking(booking)
                .amount(new BigDecimal("4000.00"))
                .refundedAmount(BigDecimal.ZERO)
                .currency("INR")
                .paymentMethod(PaymentMethod.RAZORPAY)
                .status(PaymentStatus.SUCCESS)
                .razorpayOrderId("order_hist_ok")
                .razorpayPaymentId("pay_hist_ok")
                .transactionReference("pay_hist_ok")
                .paidAt(LocalDateTime.now())
                .invoiceNumber("INV-HIST-0001")
                .build());

        var forBooking = paymentRepository.findByBookingId(booking.getId());
        assertThat(forBooking).hasSize(2);

        var successOnly = paymentRepository.findByStatus(PaymentStatus.SUCCESS).stream()
                .filter(p -> p.getBooking().getId().equals(booking.getId()))
                .toList();
        assertThat(successOnly).hasSize(1);
        assertThat(successOnly.getFirst().getRazorpayOrderId()).isEqualTo("order_hist_ok");

        assertThat(paymentRepository.existsByBookingIdAndStatus(booking.getId(), PaymentStatus.SUCCESS)).isTrue();
        assertThat(paymentRepository.existsByBookingIdAndStatus(booking.getId(), PaymentStatus.FAILED)).isFalse();
    }
}
