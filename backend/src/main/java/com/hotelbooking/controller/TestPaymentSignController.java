package com.hotelbooking.controller;

import com.hotelbooking.dto.SignPaymentRequest;
import com.hotelbooking.dto.SignPaymentResponse;
import com.hotelbooking.payment.MockRazorpayGateway;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes mock checkout signing <b>only</b> when {@code spring.profiles.active=test}.
 * Never enable in production — signing belongs on the client test harness / MockMvc only.
 */
@RestController
@RequestMapping("/payments/test")
@Profile("test")
@RequiredArgsConstructor
@Hidden
public class TestPaymentSignController {

    private final MockRazorpayGateway mockRazorpayGateway;

    @Operation(summary = "Test-profile only: mint a valid mock Razorpay checkout signature")
    @PostMapping("/sign")
    public ResponseEntity<SignPaymentResponse> sign(@Valid @RequestBody SignPaymentRequest request) {
        String signature = mockRazorpayGateway.signPayment(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId()
        );
        return ResponseEntity.ok(SignPaymentResponse.builder()
                .razorpayOrderId(request.getRazorpayOrderId())
                .razorpayPaymentId(request.getRazorpayPaymentId())
                .razorpaySignature(signature)
                .build());
    }
}
