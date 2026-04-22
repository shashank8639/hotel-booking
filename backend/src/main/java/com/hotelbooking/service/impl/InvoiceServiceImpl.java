package com.hotelbooking.service.impl;

import com.hotelbooking.database.PaymentStatus;
import com.hotelbooking.dto.InvoiceResponse;
import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.BookingRoom;
import com.hotelbooking.entity.Payment;
import com.hotelbooking.exception.InvoiceNotFoundException;
import com.hotelbooking.exception.PaymentValidationException;
import com.hotelbooking.repository.PaymentRepository;
import com.hotelbooking.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvoiceServiceImpl implements InvoiceService {

    public static final String HOTEL_NAME = "Grand Horizon Hotel";
    private static final BigDecimal GST_RATE = new BigDecimal("0.18");

    private final PaymentRepository paymentRepository;

    @Override
    public InvoiceResponse buildInvoice(Payment payment) {
        Booking booking = payment.getBooking();
        if (booking == null) {
            throw new PaymentValidationException("Payment has no booking");
        }

        int nights = (int) ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
        List<InvoiceResponse.InvoiceLineItem> lines = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (BookingRoom br : booking.getBookingRooms()) {
            InvoiceResponse.InvoiceLineItem line = InvoiceResponse.InvoiceLineItem.builder()
                    .roomNumber(br.getRoom().getRoomNumber())
                    .roomType(br.getRoom().getRoomType().name())
                    .pricePerNight(br.getPricePerNight())
                    .nights(br.getNumberOfNights())
                    .subtotal(br.getSubtotal())
                    .build();
            lines.add(line);
            subtotal = subtotal.add(br.getSubtotal());
        }

        // GST exclusive: room subtotals are net; GST is added on top.
        BigDecimal taxable = payment.getTaxableAmount() != null ? payment.getTaxableAmount() : subtotal;
        BigDecimal gstAmount = payment.getGstAmount() != null
                ? payment.getGstAmount()
                : taxable.multiply(GST_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = payment.getAmount() != null
                ? payment.getAmount()
                : taxable.add(gstAmount);

        return InvoiceResponse.builder()
                .invoiceNumber(payment.getInvoiceNumber())
                .hotelName(HOTEL_NAME)
                .bookingId(booking.getId())
                .paymentId(payment.getId())
                .guestName(booking.getGuest().getFirstName() + " " + booking.getGuest().getLastName())
                .guestEmail(booking.getGuest().getEmail())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .numberOfNights(nights)
                .rooms(lines)
                .subtotal(taxable)
                .gstRate(GST_RATE)
                .gstAmount(gstAmount)
                .totalAmount(total)
                .taxModel("EXCLUSIVE")
                .currency(payment.getCurrency())
                .baseCurrency(payment.getBaseCurrency())
                .fxRate(payment.getFxRate())
                .amountInBase(payment.getAmountInBase())
                .paymentStatus(payment.getStatus().name())
                .razorpayPaymentId(payment.getRazorpayPaymentId())
                .paidAt(payment.getPaidAt() != null ? payment.getPaidAt().toString() : null)
                .build();
    }

    @Override
    public InvoiceResponse getInvoiceForBooking(Long bookingId) {
        Payment payment = paymentRepository
                .findFirstByBookingIdAndStatusOrderByPaidAtDesc(bookingId, PaymentStatus.SUCCESS)
                .orElseThrow(() -> new InvoiceNotFoundException(
                        "No successful payment/invoice found for booking id: " + bookingId));
        if (payment.getInvoiceNumber() == null) {
            throw new InvoiceNotFoundException("Invoice not generated yet for booking id: " + bookingId);
        }
        return buildInvoice(payment);
    }

    @Override
    public String toHtml(InvoiceResponse invoice) {
        String roomsHtml = invoice.getRooms().stream()
                .map(r -> "<tr><td>" + r.getRoomNumber() + "</td><td>" + r.getRoomType()
                        + "</td><td>" + r.getPricePerNight() + "</td><td>" + r.getNights()
                        + "</td><td>" + r.getSubtotal() + "</td></tr>")
                .collect(Collectors.joining());

        return """
                <html><body>
                <h1>%s — Tax Invoice</h1>
                <p><b>Invoice:</b> %s</p>
                <p><b>Guest:</b> %s (%s)</p>
                <p><b>Booking:</b> #%d | %s → %s (%d nights)</p>
                <table border="1" cellpadding="6" cellspacing="0">
                <tr><th>Room</th><th>Type</th><th>Rate</th><th>Nights</th><th>Subtotal</th></tr>
                %s
                </table>
                <p>Taxable value: %s %s</p>
                <p>GST (%.0f%%): %s %s</p>
                <p><b>Total paid: %s %s</b></p>
                <p>Payment ID: %s | Status: %s | Paid at: %s</p>
                <hr/><p>Thank you for staying with us.</p>
                </body></html>
                """.formatted(
                invoice.getHotelName(),
                invoice.getInvoiceNumber(),
                invoice.getGuestName(),
                invoice.getGuestEmail(),
                invoice.getBookingId(),
                invoice.getCheckInDate(),
                invoice.getCheckOutDate(),
                invoice.getNumberOfNights(),
                roomsHtml,
                invoice.getCurrency(), invoice.getSubtotal(),
                invoice.getGstRate().multiply(BigDecimal.valueOf(100)),
                invoice.getCurrency(), invoice.getGstAmount(),
                invoice.getCurrency(), invoice.getTotalAmount(),
                invoice.getRazorpayPaymentId(),
                invoice.getPaymentStatus(),
                invoice.getPaidAt()
        );
    }

    /**
     * Minimal PDF writer (no third-party library — pom.xml must stay unchanged).
     * Produces a single-page text invoice suitable for learning / receipts.
     */
    @Override
    public byte[] toPdf(InvoiceResponse invoice) {
        String content = """
                %s
                TAX INVOICE
                Invoice: %s
                Guest: %s <%s>
                Booking #%d | %s to %s (%d nights)
                Subtotal: %s %s
                GST: %s %s
                TOTAL: %s %s
                Payment: %s (%s)
                Paid at: %s
                """.formatted(
                invoice.getHotelName(),
                invoice.getInvoiceNumber(),
                invoice.getGuestName(),
                invoice.getGuestEmail(),
                invoice.getBookingId(),
                invoice.getCheckInDate(),
                invoice.getCheckOutDate(),
                invoice.getNumberOfNights(),
                invoice.getCurrency(), invoice.getSubtotal(),
                invoice.getCurrency(), invoice.getGstAmount(),
                invoice.getCurrency(), invoice.getTotalAmount(),
                invoice.getRazorpayPaymentId(),
                invoice.getPaymentStatus(),
                invoice.getPaidAt()
        );

        return buildSimplePdf(content);
    }

    /**
     * Builds a valid one-page PDF containing the given plain text.
     */
    static byte[] buildSimplePdf(String text) {
        String escaped = text
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("\r", "");

        StringBuilder tj = new StringBuilder();
        for (String line : escaped.split("\n")) {
            tj.append("0 -14 Td (").append(line).append(") Tj\n");
        }

        String stream = "BT /F1 11 Tf 50 750 Td\n" + tj + "ET";
        byte[] streamBytes = stream.getBytes(StandardCharsets.US_ASCII);

        StringBuilder pdf = new StringBuilder();
        pdf.append("%PDF-1.4\n");
        List<Integer> offsets = new ArrayList<>();

        offsets.add(pdf.length());
        pdf.append("1 0 obj<< /Type /Catalog /Pages 2 0 R >>endobj\n");

        offsets.add(pdf.length());
        pdf.append("2 0 obj<< /Type /Pages /Kids [3 0 R] /Count 1 >>endobj\n");

        offsets.add(pdf.length());
        pdf.append("3 0 obj<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] ")
                .append("/Contents 4 0 R /Resources<< /Font<< /F1 5 0 R >> >> >>endobj\n");

        offsets.add(pdf.length());
        pdf.append("4 0 obj<< /Length ").append(streamBytes.length).append(" >>stream\n");
        pdf.append(stream);
        pdf.append("\nendstream\nendobj\n");

        offsets.add(pdf.length());
        pdf.append("5 0 obj<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>endobj\n");

        int xrefPos = pdf.length();
        pdf.append("xref\n0 ").append(offsets.size() + 1).append("\n");
        pdf.append("0000000000 65535 f \n");
        for (int offset : offsets) {
            pdf.append(String.format("%010d 00000 n \n", offset));
        }
        pdf.append("trailer<< /Size ").append(offsets.size() + 1)
                .append(" /Root 1 0 R >>\nstartxref\n").append(xrefPos).append("\n%%EOF");

        return pdf.toString().getBytes(StandardCharsets.US_ASCII);
    }
}
