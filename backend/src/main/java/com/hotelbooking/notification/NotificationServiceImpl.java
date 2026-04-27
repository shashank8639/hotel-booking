package com.hotelbooking.notification;

import com.hotelbooking.config.MailProperties;
import com.hotelbooking.database.EmailType;
import com.hotelbooking.dto.InvoiceResponse;
import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.BookingRoom;
import com.hotelbooking.entity.EmailOutbox;
import com.hotelbooking.entity.Guest;
import com.hotelbooking.entity.Payment;
import com.hotelbooking.exception.EmailValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final EmailSender emailSender;
    private final EmailTemplateEngine templateEngine;
    private final EmailUtils emailUtils;
    private final MailProperties mailProperties;
    private final EmailOutboxService emailOutboxService;
    private final EmailBounceHandler emailBounceHandler;
    private final PasswordResetEmailRateLimiter passwordResetEmailRateLimiter;

    @Override
    public void sendBookingConfirmation(Booking booking) {
        log.info("EMAIL REQUEST type=BOOKING_CONFIRMATION bookingId={}", booking.getId());
        Guest guest = booking.getGuest();
        if (!emailBounceHandler.allowsTransactional(guest)) {
            log.info("Skipping BOOKING_CONFIRMATION — guest opted out of transactional email guestId={}",
                    guest != null ? guest.getId() : null);
            return;
        }
        String to = guest.getEmail();
        emailUtils.validateRecipient(to);
        String locale = resolveLocale(guest);

        Map<String, String> vars = baseVars();
        vars.putAll(bookingVars(booking));
        String html = templateEngine.render("booking-confirmation.html", vars, locale);

        deliver(new EmailMessage()
                        .setTo(to)
                        .setSubject(emailUtils.subject("Booking Confirmed", "#" + booking.getId()))
                        .setHtmlBody(html),
                EmailType.BOOKING_CONFIRMATION,
                "booking-confirmation.html",
                locale);
    }

    @Override
    public void sendBookingCancellation(Booking booking, String refundStatus, BigDecimal refundAmount) {
        log.info("EMAIL REQUEST type=BOOKING_CANCELLATION bookingId={}", booking.getId());
        Guest guest = booking.getGuest();
        if (!emailBounceHandler.allowsTransactional(guest)) {
            log.info("Skipping BOOKING_CANCELLATION — guest opted out of transactional email guestId={}",
                    guest != null ? guest.getId() : null);
            return;
        }
        String to = guest.getEmail();
        emailUtils.validateRecipient(to);
        String locale = resolveLocale(guest);

        Map<String, String> vars = baseVars();
        vars.putAll(bookingVars(booking));
        vars.put("cancellationDate", emailUtils.formatDate(LocalDate.now()));
        vars.put("refundStatus", refundStatus == null ? "Pending review" : refundStatus);
        vars.put("refundAmount", emailUtils.formatMoney(
                refundAmount == null ? BigDecimal.ZERO : refundAmount,
                "INR"));

        String html = templateEngine.render("booking-cancellation.html", vars, locale);
        EmailMessage message = new EmailMessage()
                .setTo(to)
                .setSubject(emailUtils.subject("Booking Cancelled", "#" + booking.getId()))
                .setHtmlBody(html);
        if (StringUtils.hasText(mailProperties.getOpsEmail())) {
            message.setCc(mailProperties.getOpsEmail());
        }

        deliver(message, EmailType.BOOKING_CANCELLATION, "booking-cancellation.html", locale);
    }

    @Override
    public void sendPaymentSuccess(Payment payment, InvoiceResponse invoice) {
        log.info("EMAIL REQUEST type=PAYMENT_SUCCESS paymentId={}", payment.getId());
        Guest guest = payment.getBooking() != null ? payment.getBooking().getGuest() : null;
        if (guest != null && !emailBounceHandler.allowsTransactional(guest)) {
            log.info("Skipping PAYMENT_SUCCESS — guest opted out of transactional email");
            return;
        }
        String to = invoice.getGuestEmail();
        emailUtils.validateRecipient(to);
        String locale = resolveLocale(guest);

        Map<String, String> vars = baseVars();
        vars.put("guestName", invoice.getGuestName());
        vars.put("bookingId", String.valueOf(invoice.getBookingId()));
        vars.put("paymentId", String.valueOf(payment.getId()));
        vars.put("razorpayPaymentId", nullToDash(payment.getRazorpayPaymentId()));
        vars.put("amount", emailUtils.formatMoney(payment.getAmount(), payment.getCurrency()));
        vars.put("paidAt", emailUtils.formatDateTime(payment.getPaidAt()));
        vars.put("invoiceNumber", nullToDash(payment.getInvoiceNumber()));

        String html = templateEngine.render("payment-success.html", vars, locale);
        deliver(new EmailMessage()
                        .setTo(to)
                        .setSubject(emailUtils.subject("Payment Successful", invoice.getInvoiceNumber()))
                        .setHtmlBody(html),
                EmailType.PAYMENT_SUCCESS,
                "payment-success.html",
                locale);
    }

    @Override
    public void sendInvoiceEmail(Payment payment, InvoiceResponse invoice, byte[] pdfBytes) {
        log.info("EMAIL REQUEST type=INVOICE paymentId={}, invoice={}", payment.getId(), invoice.getInvoiceNumber());
        Guest guest = payment.getBooking() != null ? payment.getBooking().getGuest() : null;
        if (guest != null && !emailBounceHandler.allowsTransactional(guest)) {
            log.info("Skipping INVOICE — guest opted out of transactional email");
            return;
        }
        String to = invoice.getGuestEmail();
        emailUtils.validateRecipient(to);

        if (invoice.getInvoiceNumber() == null || invoice.getInvoiceNumber().isBlank()) {
            throw new EmailValidationException("Invoice number is required before sending invoice email");
        }
        if (!emailUtils.hasAttachment(pdfBytes)) {
            throw new EmailValidationException("Invoice PDF attachment is required");
        }
        log.info("ATTACHMENT GENERATED invoice={}, bytes={}", invoice.getInvoiceNumber(), pdfBytes.length);
        String locale = resolveLocale(guest);

        Map<String, String> vars = baseVars();
        vars.put("guestName", invoice.getGuestName());
        vars.put("bookingId", String.valueOf(invoice.getBookingId()));
        vars.put("invoiceNumber", invoice.getInvoiceNumber());
        vars.put("checkInDate", emailUtils.formatDate(invoice.getCheckInDate()));
        vars.put("checkOutDate", emailUtils.formatDate(invoice.getCheckOutDate()));
        vars.put("roomsSummary", roomsFromInvoice(invoice));
        vars.put("subtotal", emailUtils.formatMoney(invoice.getSubtotal(), invoice.getCurrency()));
        vars.put("gstAmount", emailUtils.formatMoney(invoice.getGstAmount(), invoice.getCurrency()));
        vars.put("totalAmount", emailUtils.formatMoney(invoice.getTotalAmount(), invoice.getCurrency()));
        vars.put("razorpayPaymentId", nullToDash(invoice.getRazorpayPaymentId()));

        String html = templateEngine.render("invoice.html", vars, locale);
        deliver(new EmailMessage()
                        .setTo(to)
                        .setSubject(emailUtils.subject("Invoice", invoice.getInvoiceNumber()))
                        .setHtmlBody(html)
                        .addAttachment(new EmailMessage.EmailAttachment(
                                invoice.getInvoiceNumber() + ".pdf",
                                "application/pdf",
                                pdfBytes
                        )),
                EmailType.INVOICE,
                "invoice.html",
                locale);
    }

    @Override
    public void sendPasswordReset(String toEmail, String resetToken) {
        log.info("EMAIL REQUEST type=PASSWORD_RESET to={}", toEmail);
        emailUtils.validateRecipient(toEmail);
        if (!passwordResetEmailRateLimiter.tryAcquire(toEmail)) {
            log.warn("PASSWORD_RESET suppressed by rate limit to={}", toEmail);
            return;
        }
        Map<String, String> vars = baseVars();
        vars.put("resetToken", resetToken);
        String locale = mailProperties.getDefaultLocale();
        String html = templateEngine.render("password-reset.html", vars, locale);
        deliver(new EmailMessage()
                        .setTo(toEmail)
                        .setSubject(emailUtils.subject("Password Reset", mailProperties.getHotelName()))
                        .setHtmlBody(html),
                EmailType.PASSWORD_RESET,
                "password-reset.html",
                locale);
    }

    @Override
    public void sendMarketingPromo(Guest guest, String subject, String promoBodyHtml) {
        log.info("EMAIL REQUEST type=MARKETING guestId={}", guest != null ? guest.getId() : null);
        if (!emailBounceHandler.allowsMarketing(guest)) {
            log.info("Skipping MARKETING — guest has not opted in to marketing email");
            return;
        }
        String to = guest.getEmail();
        emailUtils.validateRecipient(to);
        String locale = resolveLocale(guest);
        Map<String, String> vars = baseVars();
        vars.put("guestName", guest.getFirstName() + " " + guest.getLastName());
        vars.put("promoBody", promoBodyHtml == null ? "" : promoBodyHtml);
        String html = templateEngine.render("marketing-promo.html", vars, locale);
        deliver(new EmailMessage()
                        .setTo(to)
                        .setSubject(emailUtils.subject(subject, mailProperties.getHotelName()))
                        .setHtmlBody(html),
                EmailType.MARKETING,
                "marketing-promo.html",
                locale);
    }

    private void deliver(EmailMessage message, EmailType type, String templateName, String locale) {
        if (emailBounceHandler.isSuppressed(message.getTo())) {
            log.warn("EMAIL SUPPRESSED (bounce list) to={}, type={}", message.getTo(), type);
            EmailOutbox suppressed = emailOutboxService.enqueue(message, type, templateName, locale);
            emailOutboxService.markSuppressed(suppressed.getId(), "Recipient on bounce/complaint suppression list");
            return;
        }

        EmailOutbox outbox = emailOutboxService.enqueue(message, type, templateName, locale);
        try {
            emailSender.send(message);
            emailOutboxService.markSent(outbox.getId());
        } catch (RuntimeException ex) {
            emailOutboxService.markFailed(outbox.getId(), ex.getMessage());
            throw ex;
        }
    }

    private String resolveLocale(Guest guest) {
        if (guest != null && StringUtils.hasText(guest.getPreferredLocale())) {
            return guest.getPreferredLocale();
        }
        return mailProperties.getDefaultLocale();
    }

    private Map<String, String> baseVars() {
        Map<String, String> vars = new HashMap<>();
        vars.put("hotelName", mailProperties.getHotelName());
        vars.put("supportEmail", mailProperties.getSupportEmail());
        return vars;
    }

    private Map<String, String> bookingVars(Booking booking) {
        Map<String, String> vars = new HashMap<>();
        vars.put("guestName", booking.getGuest().getFirstName() + " " + booking.getGuest().getLastName());
        vars.put("bookingId", String.valueOf(booking.getId()));
        vars.put("bookingStatus", booking.getStatus().name());
        vars.put("checkInDate", emailUtils.formatDate(booking.getCheckInDate()));
        vars.put("checkOutDate", emailUtils.formatDate(booking.getCheckOutDate()));
        long nights = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
        vars.put("numberOfNights", String.valueOf(nights));
        vars.put("roomsSummary", roomsFromBooking(booking));
        vars.put("totalAmount", emailUtils.formatMoney(booking.getTotalAmount(), "INR"));
        return vars;
    }

    private String roomsFromBooking(Booking booking) {
        if (booking.getBookingRooms() == null || booking.getBookingRooms().isEmpty()) {
            return "-";
        }
        return booking.getBookingRooms().stream()
                .map(BookingRoom::getRoom)
                .map(r -> r.getRoomNumber() + " (" + r.getRoomType() + ")")
                .collect(Collectors.joining(", "));
    }

    private String roomsFromInvoice(InvoiceResponse invoice) {
        if (invoice.getRooms() == null || invoice.getRooms().isEmpty()) {
            return "-";
        }
        return invoice.getRooms().stream()
                .map(r -> r.getRoomNumber() + " (" + r.getRoomType() + ")")
                .collect(Collectors.joining(", "));
    }

    private static String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
