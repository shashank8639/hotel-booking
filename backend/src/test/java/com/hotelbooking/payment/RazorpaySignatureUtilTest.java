package com.hotelbooking.payment;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RazorpaySignatureUtilTest {

    @Test
    void paymentSignature_shouldValidateHmac() {
        String secret = "test_secret";
        String orderId = "order_ABC";
        String paymentId = "pay_XYZ";
        String signature = RazorpaySignatureUtil.hmacSha256Hex(secret, orderId + "|" + paymentId);

        assertThat(RazorpaySignatureUtil.isPaymentSignatureValid(secret, orderId, paymentId, signature)).isTrue();
        assertThat(RazorpaySignatureUtil.isPaymentSignatureValid(secret, orderId, paymentId, "tampered")).isFalse();
    }

    @Test
    void webhookSignature_shouldValidateBodyHmac() {
        String secret = "whsec";
        String body = "{\"event\":\"payment.captured\"}";
        String signature = RazorpaySignatureUtil.hmacSha256Hex(secret, body);

        assertThat(RazorpaySignatureUtil.isWebhookSignatureValid(secret, body, signature)).isTrue();
    }
}
