package com.hotelbooking.payment;

import java.math.BigDecimal;

/**
 * Abstraction over Razorpay HTTP API so services stay testable and mockable locally
 * without adding the official Razorpay SDK (pom.xml must stay unchanged).
 */
public interface RazorpayGateway {

    RazorpayOrderResult createOrder(BigDecimal amountInMajorUnits, String currency, String receipt);

    RazorpayRefundResult refund(String razorpayPaymentId, BigDecimal amountInMajorUnits, String currency);

    boolean verifyPaymentSignature(String orderId, String paymentId, String signature);

    boolean verifyWebhookSignature(String rawBody, String signatureHeader);
}
