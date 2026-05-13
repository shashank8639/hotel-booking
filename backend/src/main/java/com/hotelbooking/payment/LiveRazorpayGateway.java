package com.hotelbooking.payment;

import com.hotelbooking.config.RazorpayProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.UUID;

/**
 * Live Razorpay REST client using Spring {@link RestClient} (no extra Maven dependency).
 * Amounts are sent in the smallest currency unit (paise for INR).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.razorpay", name = "mock-enabled", havingValue = "false")
public class LiveRazorpayGateway implements RazorpayGateway {

    private final RazorpayProperties properties;

    private RestClient client() {
        return RestClient.builder()
                .baseUrl(properties.getApiBaseUrl())
                .defaultHeaders(headers -> headers.setBasicAuth(properties.getKeyId(), properties.getKeySecret()))
                .build();
    }

    @Override
    public RazorpayOrderResult createOrder(BigDecimal amountInMajorUnits, String currency, String receipt) {
        long amountPaise = toMinorUnits(amountInMajorUnits);
        log.info("Creating Razorpay order amountPaise={}, currency={}, receipt={}", amountPaise, currency, receipt);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = client().post()
                .uri("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "amount", amountPaise,
                        "currency", currency,
                        "receipt", receipt,
                        "payment_capture", 1
                ))
                .retrieve()
                .body(Map.class);

        if (response == null || response.get("id") == null) {
            throw new IllegalStateException("Razorpay order creation returned empty response");
        }

        return new RazorpayOrderResult(
                String.valueOf(response.get("id")),
                amountInMajorUnits,
                currency,
                receipt,
                String.valueOf(response.getOrDefault("status", "created"))
        );
    }

    @Override
    public RazorpayRefundResult refund(String razorpayPaymentId, BigDecimal amountInMajorUnits, String currency) {
        long amountPaise = toMinorUnits(amountInMajorUnits);
        log.info("Creating Razorpay refund paymentId={}, amountPaise={}", razorpayPaymentId, amountPaise);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = client().post()
                .uri("/payments/{paymentId}/refund", razorpayPaymentId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("amount", amountPaise))
                .retrieve()
                .body(Map.class);

        if (response == null || response.get("id") == null) {
            throw new IllegalStateException("Razorpay refund returned empty response");
        }

        return new RazorpayRefundResult(
                String.valueOf(response.get("id")),
                razorpayPaymentId,
                amountInMajorUnits,
                String.valueOf(response.getOrDefault("status", "processed"))
        );
    }

    @Override
    public boolean verifyPaymentSignature(String orderId, String paymentId, String signature) {
        return RazorpaySignatureUtil.isPaymentSignatureValid(properties.getKeySecret(), orderId, paymentId, signature);
    }

    @Override
    public boolean verifyWebhookSignature(String rawBody, String signatureHeader) {
        return RazorpaySignatureUtil.isWebhookSignatureValid(properties.getWebhookSecret(), rawBody, signatureHeader);
    }

    private static long toMinorUnits(BigDecimal major) {
        return major.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }
}
