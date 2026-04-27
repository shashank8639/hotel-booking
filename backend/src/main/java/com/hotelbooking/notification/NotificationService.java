package com.hotelbooking.notification;

import com.hotelbooking.dto.InvoiceResponse;
import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.Guest;
import com.hotelbooking.entity.Payment;

/**
 * Application-facing notification API (booking / payment / invoice emails).
 */
public interface NotificationService {

    void sendBookingConfirmation(Booking booking);

    void sendBookingCancellation(Booking booking, String refundStatus, java.math.BigDecimal refundAmount);

    void sendPaymentSuccess(Payment payment, InvoiceResponse invoice);

    void sendInvoiceEmail(Payment payment, InvoiceResponse invoice, byte[] pdfBytes);

    void sendPasswordReset(String toEmail, String resetToken);

    /**
     * Marketing / promo mail — only sent when the guest has opted in ({@code marketingEmailsEnabled}).
     */
    void sendMarketingPromo(Guest guest, String subject, String promoBodyHtml);
}
