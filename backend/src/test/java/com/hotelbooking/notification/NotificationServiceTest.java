package com.hotelbooking.notification;

import com.hotelbooking.config.MailProperties;
import com.hotelbooking.database.BookingStatus;
import com.hotelbooking.database.EmailSuppressionReason;
import com.hotelbooking.database.EmailType;
import com.hotelbooking.database.PaymentMethod;
import com.hotelbooking.database.PaymentStatus;
import com.hotelbooking.database.RoomType;
import com.hotelbooking.dto.InvoiceResponse;
import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.BookingRoom;
import com.hotelbooking.entity.EmailOutbox;
import com.hotelbooking.entity.Guest;
import com.hotelbooking.entity.Payment;
import com.hotelbooking.entity.Room;
import com.hotelbooking.exception.EmailValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private EmailSender emailSender;

    @Mock
    private EmailTemplateEngine templateEngine;

    @Mock
    private MailProperties mailProperties;

    @Mock
    private EmailOutboxService emailOutboxService;

    @Mock
    private EmailBounceHandler emailBounceHandler;

    @Mock
    private PasswordResetEmailRateLimiter passwordResetEmailRateLimiter;

    private NotificationServiceImpl notificationService;

    private final EmailUtils emailUtils = new EmailUtils();

    private Booking booking;
    private Payment payment;
    private InvoiceResponse invoice;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl(
                emailSender,
                templateEngine,
                emailUtils,
                mailProperties,
                emailOutboxService,
                emailBounceHandler,
                passwordResetEmailRateLimiter
        );

        lenient().when(mailProperties.getHotelName()).thenReturn("Grand Horizon Hotel");
        lenient().when(mailProperties.getSupportEmail()).thenReturn("support@grandhorizon.example");
        lenient().when(mailProperties.getOpsEmail()).thenReturn("ops@grandhorizon.example");
        lenient().when(mailProperties.getDefaultLocale()).thenReturn("en");
        lenient().when(templateEngine.render(any(), anyMap(), anyString())).thenReturn("<html>ok</html>");
        lenient().when(emailBounceHandler.allowsTransactional(any())).thenReturn(true);
        lenient().when(emailBounceHandler.allowsMarketing(any())).thenReturn(false);
        lenient().when(emailBounceHandler.isSuppressed(anyString())).thenReturn(false);
        lenient().when(passwordResetEmailRateLimiter.tryAcquire(anyString())).thenReturn(true);

        EmailOutbox outbox = EmailOutbox.builder().emailType(EmailType.OTHER).toAddress("x").subject("s").build();
        outbox.setId(1L);
        lenient().when(emailOutboxService.enqueue(any(), any(), any(), any())).thenReturn(outbox);

        Guest guest = Guest.builder()
                .firstName("Asha")
                .lastName("Patel")
                .email("asha@example.com")
                .preferredLocale("en")
                .transactionalEmailsEnabled(true)
                .marketingEmailsEnabled(false)
                .build();
        guest.setId(1L);
        Room room = Room.builder().roomNumber("101").roomType(RoomType.STANDARD).capacity(2)
                .pricePerNight(new BigDecimal("2500")).build();
        room.setId(1L);
        booking = Booking.builder()
                .guest(guest)
                .checkInDate(LocalDate.now().plusDays(3))
                .checkOutDate(LocalDate.now().plusDays(5))
                .status(BookingStatus.CONFIRMED)
                .totalAmount(new BigDecimal("5000.00"))
                .build();
        booking.setId(10L);
        booking.addBookingRoom(BookingRoom.builder()
                .room(room)
                .pricePerNight(new BigDecimal("2500"))
                .numberOfNights(2)
                .subtotal(new BigDecimal("5000"))
                .build());

        payment = Payment.builder()
                .booking(booking)
                .amount(new BigDecimal("5000.00"))
                .currency("INR")
                .paymentMethod(PaymentMethod.RAZORPAY)
                .status(PaymentStatus.SUCCESS)
                .razorpayPaymentId("pay_1")
                .invoiceNumber("INV-1")
                .paidAt(LocalDateTime.now())
                .build();
        payment.setId(100L);

        invoice = InvoiceResponse.builder()
                .invoiceNumber("INV-1")
                .guestName("Asha Patel")
                .guestEmail("asha@example.com")
                .bookingId(10L)
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .subtotal(new BigDecimal("4237.29"))
                .gstAmount(new BigDecimal("762.71"))
                .totalAmount(new BigDecimal("5000.00"))
                .currency("INR")
                .razorpayPaymentId("pay_1")
                .rooms(java.util.List.of(InvoiceResponse.InvoiceLineItem.builder()
                        .roomNumber("101").roomType("STANDARD").build()))
                .build();
    }

    @Test
    void sendBookingConfirmation_shouldRenderAndSend() {
        notificationService.sendBookingConfirmation(booking);

        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailSender).send(captor.capture());
        assertThat(captor.getValue().getTo()).isEqualTo("asha@example.com");
        assertThat(captor.getValue().getSubject()).contains("Booking Confirmed");
        verify(templateEngine).render(eq("booking-confirmation.html"), anyMap(), eq("en"));
        verify(emailOutboxService).markSent(1L);
    }

    @Test
    void sendBookingCancellation_shouldCcHotelOps() {
        notificationService.sendBookingCancellation(booking, "Pending finance review", BigDecimal.ZERO);

        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailSender).send(captor.capture());
        assertThat(captor.getValue().getCc()).isEqualTo("ops@grandhorizon.example");
        verify(templateEngine).render(eq("booking-cancellation.html"), anyMap(), eq("en"));
    }

    @Test
    void sendBookingCancellation_shouldUseGuestLocale() {
        booking.getGuest().setPreferredLocale("hi");
        notificationService.sendBookingCancellation(booking, "Pending", BigDecimal.ZERO);
        verify(templateEngine).render(eq("booking-cancellation.html"), anyMap(), eq("hi"));
    }

    @Test
    void sendPaymentSuccess_shouldSend() {
        notificationService.sendPaymentSuccess(payment, invoice);
        verify(templateEngine).render(eq("payment-success.html"), anyMap(), anyString());
        verify(emailSender).send(any(EmailMessage.class));
    }

    @Test
    void sendInvoiceEmail_shouldAttachPdf() {
        byte[] pdf = "%PDF-1.4".getBytes();
        notificationService.sendInvoiceEmail(payment, invoice, pdf);

        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailSender).send(captor.capture());
        assertThat(captor.getValue().getAttachments()).hasSize(1);
        assertThat(captor.getValue().getAttachments().get(0).filename()).isEqualTo("INV-1.pdf");
    }

    @Test
    void sendInvoiceEmail_shouldRejectMissingPdf() {
        assertThatThrownBy(() -> notificationService.sendInvoiceEmail(payment, invoice, null))
                .isInstanceOf(EmailValidationException.class);
    }

    @Test
    void sendBookingConfirmation_shouldRejectInvalidEmail() {
        booking.getGuest().setEmail("not-an-email");
        assertThatThrownBy(() -> notificationService.sendBookingConfirmation(booking))
                .isInstanceOf(EmailValidationException.class);
    }

    @Test
    void sendPasswordReset_shouldSkipWhenRateLimited() {
        when(passwordResetEmailRateLimiter.tryAcquire("asha@example.com")).thenReturn(false);
        notificationService.sendPasswordReset("asha@example.com", "token");
        verify(emailSender, never()).send(any());
    }

    @Test
    void sendMarketingPromo_shouldSkipWhenNotOptedIn() {
        when(emailBounceHandler.allowsMarketing(booking.getGuest())).thenReturn(false);
        notificationService.sendMarketingPromo(booking.getGuest(), "Deal", "<p>Hi</p>");
        verify(emailSender, never()).send(any());
    }

    @Test
    void sendMarketingPromo_shouldSendWhenOptedIn() {
        when(emailBounceHandler.allowsMarketing(booking.getGuest())).thenReturn(true);
        notificationService.sendMarketingPromo(booking.getGuest(), "Deal", "<p>Hi</p>");
        verify(emailSender).send(any(EmailMessage.class));
        verify(templateEngine).render(eq("marketing-promo.html"), anyMap(), anyString());
    }

    @Test
    void deliver_shouldSuppressBouncedRecipients() {
        when(emailBounceHandler.isSuppressed("asha@example.com")).thenReturn(true);
        notificationService.sendBookingConfirmation(booking);
        verify(emailSender, never()).send(any());
        verify(emailOutboxService).markSuppressed(eq(1L), anyString());
    }
}
