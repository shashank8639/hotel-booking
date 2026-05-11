package com.hotelbooking.payment;

import com.hotelbooking.config.RazorpayProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MockRazorpayGatewayTest {

    private MockRazorpayGateway gateway;

    @BeforeEach
    void setUp() {
        RazorpayProperties properties = new RazorpayProperties();
        properties.setMockEnabled(true);
        properties.setKeySecret("mock_razorpay_secret");
        properties.setWebhookSecret("mock_webhook_secret");
        gateway = new MockRazorpayGateway(properties);
    }

    @Test
    void createOrder_returnsMockOrderId() {
        RazorpayOrderResult order = gateway.createOrder(new BigDecimal("1500.00"), "INR", "rcpt-1");

        assertThat(order.orderId()).startsWith("order_mock_");
        assertThat(order.amount()).isEqualByComparingTo("1500.00");
        assertThat(order.currency()).isEqualTo("INR");
        assertThat(order.status()).isEqualTo("created");
    }

    @Test
    void signPayment_andVerify_roundTrip() {
        String orderId = "order_mock_1";
        String paymentId = "pay_mock_1";
        String signature = gateway.signPayment(orderId, paymentId);

        assertThat(gateway.verifyPaymentSignature(orderId, paymentId, signature)).isTrue();
        assertThat(gateway.verifyPaymentSignature(orderId, paymentId, "deadbeef")).isFalse();
    }

    @Test
    void refund_returnsProcessedRefund() {
        RazorpayRefundResult refund = gateway.refund("pay_1", new BigDecimal("100.00"), "INR");

        assertThat(refund.refundId()).startsWith("rfnd_mock_");
        assertThat(refund.paymentId()).isEqualTo("pay_1");
        assertThat(refund.status()).isEqualTo("processed");
    }
}
