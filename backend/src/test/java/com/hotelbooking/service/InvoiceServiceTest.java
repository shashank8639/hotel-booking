package com.hotelbooking.service;

import com.hotelbooking.database.BookingStatus;
import com.hotelbooking.database.PaymentMethod;
import com.hotelbooking.database.PaymentStatus;
import com.hotelbooking.database.RoomType;
import com.hotelbooking.dto.InvoiceResponse;
import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.BookingRoom;
import com.hotelbooking.entity.Guest;
import com.hotelbooking.entity.Payment;
import com.hotelbooking.entity.Room;
import com.hotelbooking.repository.PaymentRepository;
import com.hotelbooking.service.impl.InvoiceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    private Payment payment;

    @BeforeEach
    void setUp() {
        Guest guest = Guest.builder().firstName("Asha").lastName("Patel").email("asha@example.com").build();
        guest.setId(1L);

        Room room = Room.builder()
                .roomNumber("101")
                .roomType(RoomType.STANDARD)
                .capacity(2)
                .pricePerNight(new BigDecimal("2500.00"))
                .build();
        room.setId(1L);

        Booking booking = Booking.builder()
                .guest(guest)
                .checkInDate(LocalDate.of(2026, 9, 1))
                .checkOutDate(LocalDate.of(2026, 9, 3))
                .status(BookingStatus.CONFIRMED)
                .totalAmount(new BigDecimal("5000.00"))
                .build();
        booking.setId(10L);

        BookingRoom line = BookingRoom.builder()
                .room(room)
                .pricePerNight(new BigDecimal("2500.00"))
                .numberOfNights(2)
                .subtotal(new BigDecimal("5000.00"))
                .build();
        booking.addBookingRoom(line);

        payment = Payment.builder()
                .booking(booking)
                .amount(new BigDecimal("5900.00"))
                .taxableAmount(new BigDecimal("5000.00"))
                .gstAmount(new BigDecimal("900.00"))
                .refundedAmount(BigDecimal.ZERO)
                .currency("INR")
                .paymentMethod(PaymentMethod.RAZORPAY)
                .status(PaymentStatus.SUCCESS)
                .razorpayPaymentId("pay_1")
                .invoiceNumber("INV-20260901-0001")
                .invoiceGeneratedAt(LocalDateTime.now())
                .paidAt(LocalDateTime.now())
                .build();
        payment.setId(100L);
    }

    @Test
    void buildInvoice_shouldUseExclusiveGstModel() {
        InvoiceResponse invoice = invoiceService.buildInvoice(payment);

        assertThat(invoice.getInvoiceNumber()).isEqualTo("INV-20260901-0001");
        assertThat(invoice.getRooms()).hasSize(1);
        assertThat(invoice.getTaxModel()).isEqualTo("EXCLUSIVE");
        assertThat(invoice.getSubtotal()).isEqualByComparingTo("5000.00");
        assertThat(invoice.getGstAmount()).isEqualByComparingTo("900.00");
        assertThat(invoice.getTotalAmount()).isEqualByComparingTo("5900.00");
        assertThat(invoice.getSubtotal().add(invoice.getGstAmount()))
                .isEqualByComparingTo(invoice.getTotalAmount());
    }

    @Test
    void toPdf_shouldProducePdfHeader() {
        InvoiceResponse invoice = invoiceService.buildInvoice(payment);
        byte[] pdf = invoiceService.toPdf(invoice);

        assertThat(new String(pdf)).startsWith("%PDF");
    }

    @Test
    void getInvoiceForBooking_shouldLoadSuccessfulPayment() {
        when(paymentRepository.findFirstByBookingIdAndStatusOrderByPaidAtDesc(10L, PaymentStatus.SUCCESS))
                .thenReturn(Optional.of(payment));

        InvoiceResponse invoice = invoiceService.getInvoiceForBooking(10L);

        assertThat(invoice.getBookingId()).isEqualTo(10L);
    }
}
