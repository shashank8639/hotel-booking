package com.hotelbooking.payment;

import java.math.BigDecimal;

/**
 * Gateway order created in Razorpay (or mock).
 */
public record RazorpayOrderResult(
        String orderId,
        BigDecimal amount,
        String currency,
        String receipt,
        String status
) {
}
