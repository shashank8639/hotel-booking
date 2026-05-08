package com.hotelbooking.service;

import com.hotelbooking.database.PaymentMethod;
import com.hotelbooking.database.PaymentStatus;
import com.hotelbooking.dto.InvoiceResponse;
import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.Payment;
import com.hotelbooking.notification.AsyncNotificationFacade;
import com.hotelbooking.service.impl.PaymentReceiptServiceImpl;
import com.hotelbooking.util.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Mockito unit test with ArgumentCaptor — verifies async receipt dispatch contract.
 */
@ExtendWith(MockitoExtension.class)
class PaymentReceiptServiceTest {

    @Mock
    private AsyncNotificationFacade asyncNotificationFacade;

    @InjectMocks
    private PaymentReceiptServiceImpl paymentReceiptService;

    @Test
    void sendPaymentReceiptAsync_delegatesToFacadeWithPaymentIdAndPdf() {
        var guest = TestDataFactory.guest("receipt@example.com");
        guest.setId(1L);
        Booking booking = TestDataFactory.pendingBooking(
                guest, LocalDate.now().plusDays(1), LocalDate.now().plusDays(2), new BigDecimal("5000"));
        booking.setId(11L);

        Payment payment = Payment.builder()
                .booking(booking)
                .amount(new BigDecimal("5000"))
                .paymentMethod(PaymentMethod.RAZORPAY)
                .status(PaymentStatus.SUCCESS)
                .build();
        payment.setId(99L);

        InvoiceResponse invoice = InvoiceResponse.builder()
                .invoiceNumber("INV-2026-0001")
                .bookingId(11L)
                .build();
        byte[] pdf = new byte[] {1, 2, 3};

        paymentReceiptService.sendPaymentReceiptAsync(payment, invoice, pdf);

        ArgumentCaptor<Long> paymentIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<InvoiceResponse> invoiceCaptor = ArgumentCaptor.forClass(InvoiceResponse.class);
        ArgumentCaptor<byte[]> pdfCaptor = ArgumentCaptor.forClass(byte[].class);

        verify(asyncNotificationFacade).paymentAndInvoiceAsync(
                paymentIdCaptor.capture(),
                invoiceCaptor.capture(),
                pdfCaptor.capture()
        );

        assertThat(paymentIdCaptor.getValue()).isEqualTo(99L);
        assertThat(invoiceCaptor.getValue().getInvoiceNumber()).isEqualTo("INV-2026-0001");
        assertThat(pdfCaptor.getValue()).containsExactly(1, 2, 3);
        verify(asyncNotificationFacade).paymentAndInvoiceAsync(eq(99L), eq(invoice), eq(pdf));
    }
}
