package com.hotelbooking.service;

import com.hotelbooking.dto.InvoiceResponse;
import com.hotelbooking.entity.Payment;

/**
 * Builds invoice models after successful payment.
 */
public interface InvoiceService {

    InvoiceResponse buildInvoice(Payment payment);

    InvoiceResponse getInvoiceForBooking(Long bookingId);

    String toHtml(InvoiceResponse invoice);

    byte[] toPdf(InvoiceResponse invoice);
}
