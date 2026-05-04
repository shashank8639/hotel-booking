package com.hotelbooking.payment;

import java.math.BigDecimal;

public record RazorpayRefundResult(
        String refundId,
        String paymentId,
        BigDecimal amount,
        String status
) {
}
