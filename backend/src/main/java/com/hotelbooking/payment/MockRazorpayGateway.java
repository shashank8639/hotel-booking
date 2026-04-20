package com.hotelbooking.payment;

import com.hotelbooking.config.RazorpayProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Local/demo Razorpay stand-in when {@code app.razorpay.mock-enabled=true} (default).
 * Signatures are computed with the configured key secret (or a fixed demo secret).
 * <p>
 * Order ids must be unique across JVM restarts — a restarting counter collides with
 * {@code payments.uk_payments_transaction_reference} from earlier demo runs.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.razorpay", name = "mock-enabled", havingValue = "true", matchIfMissing = true)
public class MockRazorpayGateway implements RazorpayGateway {

    private final RazorpayProperties properties;

    @Override
    public RazorpayOrderResult createOrder(BigDecimal amountInMajorUnits, String currency, String receipt) {
        String orderId = "order_mock_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        log.info("Mock Razorpay order created id={}, amount={}, receipt={}", orderId, amountInMajorUnits, receipt);
        return new RazorpayOrderResult(orderId, amountInMajorUnits, currency, receipt, "created");
    }

    @Override
    public RazorpayRefundResult refund(String razorpayPaymentId, BigDecimal amountInMajorUnits, String currency) {
        String refundId = "rfnd_mock_" + UUID.randomUUID().toString().substring(0, 8);
        log.info("Mock Razorpay refund id={}, paymentId={}, amount={}", refundId, razorpayPaymentId, amountInMajorUnits);
        return new RazorpayRefundResult(refundId, razorpayPaymentId, amountInMajorUnits, "processed");
    }

    @Override
    public boolean verifyPaymentSignature(String orderId, String paymentId, String signature) {
        return RazorpaySignatureUtil.isPaymentSignatureValid(secret(), orderId, paymentId, signature);
    }

    @Override
    public boolean verifyWebhookSignature(String rawBody, String signatureHeader) {
        String webhookSecret = properties.getWebhookSecret();
        if (webhookSecret == null || webhookSecret.isBlank()) {
            webhookSecret = secret();
        }
        return RazorpaySignatureUtil.isWebhookSignatureValid(webhookSecret, rawBody, signatureHeader);
    }

    /** Helper for tests/clients to mint a valid mock checkout signature. */
    public String signPayment(String orderId, String paymentId) {
        return RazorpaySignatureUtil.hmacSha256Hex(secret(), orderId + "|" + paymentId);
    }

    private String secret() {
        String keySecret = properties.getKeySecret();
        return (keySecret == null || keySecret.isBlank()) ? "mock_razorpay_secret" : keySecret;
    }
}
