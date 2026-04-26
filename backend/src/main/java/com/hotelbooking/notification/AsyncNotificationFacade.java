package com.hotelbooking.notification;

import com.hotelbooking.dto.InvoiceResponse;
import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.Payment;
import com.hotelbooking.repository.BookingRepository;
import com.hotelbooking.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Async façade so booking/payment transactions are not blocked by email I/O.
 * Reloads aggregates inside a read-only transaction to avoid LazyInitializationException.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncNotificationFacade {

    private final NotificationService notificationService;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    @Async
    @Transactional(readOnly = true)
    public void bookingCancellationAsync(Long bookingId, String refundStatus, BigDecimal refundAmount) {
        try {
            Booking booking = loadBookingGraph(bookingId);
            notificationService.sendBookingCancellation(booking, refundStatus, refundAmount);
        } catch (Exception ex) {
            log.error("EMAIL FAILED type=BOOKING_CANCELLATION bookingId={}, reason={}",
                    bookingId, ex.getMessage(), ex);
            log.warn("EMAIL RETRY candidate type=BOOKING_CANCELLATION bookingId={}", bookingId);
        }
    }

    @Async
    @Transactional(readOnly = true)
    public void paymentAndInvoiceAsync(Long paymentId, InvoiceResponse invoice, byte[] pdfBytes) {
        try {
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));
            // Initialize graph for confirmation email
            Booking booking = payment.getBooking();
            booking.getGuest().getEmail();
            if (booking.getBookingRooms() != null) {
                booking.getBookingRooms().forEach(br -> br.getRoom().getRoomNumber());
            }

            notificationService.sendPaymentSuccess(payment, invoice);
            notificationService.sendBookingConfirmation(booking);
            notificationService.sendInvoiceEmail(payment, invoice, pdfBytes);
        } catch (Exception ex) {
            log.error("EMAIL FAILED type=PAYMENT_FLOW paymentId={}, reason={}",
                    paymentId, ex.getMessage(), ex);
            log.warn("EMAIL RETRY candidate type=PAYMENT_FLOW paymentId={}", paymentId);
        }
    }

    @Async
    public void passwordResetAsync(String toEmail, String resetToken) {
        try {
            notificationService.sendPasswordReset(toEmail, resetToken);
        } catch (Exception ex) {
            log.error("EMAIL FAILED type=PASSWORD_RESET to={}, reason={}", toEmail, ex.getMessage(), ex);
        }
    }

    private Booking loadBookingGraph(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));
        booking.getGuest().getEmail();
        if (booking.getBookingRooms() != null) {
            booking.getBookingRooms().forEach(br -> br.getRoom().getRoomNumber());
        }
        return booking;
    }
}
