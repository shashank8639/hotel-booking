package com.hotelbooking.service;

import com.hotelbooking.database.PaymentStatus;
import com.hotelbooking.dto.CreatePaymentOrderRequest;
import com.hotelbooking.dto.CreatePaymentOrderResponse;
import com.hotelbooking.dto.InvoiceResponse;
import com.hotelbooking.dto.PaymentResponse;
import com.hotelbooking.dto.RefundPaymentRequest;
import com.hotelbooking.dto.VerifyPaymentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface PaymentService {

    CreatePaymentOrderResponse createOrder(CreatePaymentOrderRequest request);

    PaymentResponse verifyPayment(VerifyPaymentRequest request);

    PaymentResponse refund(RefundPaymentRequest request);

    PaymentResponse getPaymentById(Long id);

    Page<PaymentResponse> getPaymentHistory(
            Long bookingId,
            Long guestId,
            PaymentStatus status,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable
    );

    void handleWebhook(String rawBody, String signatureHeader);

    InvoiceResponse getInvoice(Long bookingId);

    byte[] getInvoicePdf(Long bookingId);
}
