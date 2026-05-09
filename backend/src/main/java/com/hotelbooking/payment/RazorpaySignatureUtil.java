package com.hotelbooking.payment;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * HMAC-SHA256 helpers for Razorpay payment and webhook signature verification.
 * <p>
 * Payment signature payload: {@code orderId + "|" + paymentId}<br>
 * Webhook signature: HMAC of raw request body with webhook secret.
 */
public final class RazorpaySignatureUtil {

    private static final String HMAC_SHA256 = "HmacSHA256";

    private RazorpaySignatureUtil() {
    }

    public static String hmacSha256Hex(String secret, String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            byte[] raw = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(raw);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to compute HMAC-SHA256", ex);
        }
    }

    public static boolean isPaymentSignatureValid(String secret, String orderId, String paymentId, String signature) {
        if (secret == null || orderId == null || paymentId == null || signature == null) {
            return false;
        }
        String expected = hmacSha256Hex(secret, orderId + "|" + paymentId);
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8)
        );
    }

    public static boolean isWebhookSignatureValid(String webhookSecret, String body, String signatureHeader) {
        if (webhookSecret == null || webhookSecret.isBlank() || body == null || signatureHeader == null) {
            return false;
        }
        String expected = hmacSha256Hex(webhookSecret, body);
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                signatureHeader.getBytes(StandardCharsets.UTF_8)
        );
    }
}
