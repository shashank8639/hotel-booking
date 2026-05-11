package com.hotelbooking.service.impl;

import com.hotelbooking.dto.InvoiceResponse;
import com.hotelbooking.entity.Payment;
import com.hotelbooking.notification.AsyncNotificationFacade;
import com.hotelbooking.service.PaymentReceiptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Bridges Module 7 payment success to Module 8 email notifications.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentReceiptServiceImpl implements PaymentReceiptService {

    private final AsyncNotificationFacade asyncNotificationFacade;

    @Override
    public void sendPaymentReceiptAsync(Payment payment, InvoiceResponse invoice, byte[] pdfBytes) {
        log.info("Dispatching payment notification emails paymentId={}, invoice={}",
                payment.getId(), invoice.getInvoiceNumber());
        asyncNotificationFacade.paymentAndInvoiceAsync(payment.getId(), invoice, pdfBytes);
    }
}
