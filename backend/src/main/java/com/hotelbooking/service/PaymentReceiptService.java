package com.hotelbooking.service;

import com.hotelbooking.dto.InvoiceResponse;
import com.hotelbooking.entity.Payment;

/**
 * Sends payment confirmation + invoice asynchronously (logging stub until mail SMTP is configured).
 */
public interface PaymentReceiptService {

    void sendPaymentReceiptAsync(Payment payment, InvoiceResponse invoice, byte[] pdfBytes);
}
