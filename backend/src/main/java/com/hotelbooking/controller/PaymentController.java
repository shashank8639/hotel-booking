package com.hotelbooking.controller;

import com.hotelbooking.database.PaymentStatus;
import com.hotelbooking.dto.CreatePaymentOrderRequest;
import com.hotelbooking.dto.CreatePaymentOrderResponse;
import com.hotelbooking.dto.InvoiceResponse;
import com.hotelbooking.dto.PaymentResponse;
import com.hotelbooking.dto.RefundPaymentRequest;
import com.hotelbooking.dto.VerifyPaymentRequest;
import com.hotelbooking.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Payment Management APIs (Razorpay order, verify, refund, webhook, invoice).
 */
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Validated
@Tag(name = "Payments", description = "Payment management and Razorpay integration")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Create Razorpay order for a booking")
    @PostMapping("/create-order")
    @PreAuthorize("hasRole('ADMIN') or @bookingOwnership.canAccess(#request.bookingId)")
    public ResponseEntity<CreatePaymentOrderResponse> createOrder(
            @Valid @RequestBody CreatePaymentOrderRequest request
    ) {
        return ResponseEntity.ok(paymentService.createOrder(request));
    }

    @Operation(summary = "Verify Razorpay checkout signature and confirm payment")
    @PostMapping("/verify")
    public ResponseEntity<PaymentResponse> verify(@Valid @RequestBody VerifyPaymentRequest request) {
        return ResponseEntity.ok(paymentService.verifyPayment(request));
    }

    @Operation(summary = "Refund a successful payment (ADMIN only)")
    @PostMapping("/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> refund(@Valid @RequestBody RefundPaymentRequest request) {
        return ResponseEntity.ok(paymentService.refund(request));
    }

    @Operation(summary = "Payment history with optional filters (customers: own guest only)")
    @GetMapping("/history")
    public ResponseEntity<Page<PaymentResponse>> history(
            @RequestParam(required = false) Long bookingId,
            @RequestParam(required = false) Long guestId,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(
                paymentService.getPaymentHistory(bookingId, guestId, status, fromDate, toDate, pageable)
        );
    }

    @Operation(summary = "Razorpay webhook (public; signature verified)")
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestBody String rawBody,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature
    ) {
        paymentService.handleWebhook(rawBody, signature);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get invoice JSON for a booking")
    @GetMapping("/invoice/{bookingId}")
    @PreAuthorize("hasRole('ADMIN') or @bookingOwnership.canAccess(#bookingId)")
    public ResponseEntity<InvoiceResponse> invoice(@PathVariable Long bookingId) {
        return ResponseEntity.ok(paymentService.getInvoice(bookingId));
    }

    @Operation(summary = "Download invoice PDF for a booking")
    @GetMapping("/invoice/pdf/{bookingId}")
    @PreAuthorize("hasRole('ADMIN') or @bookingOwnership.canAccess(#bookingId)")
    public ResponseEntity<byte[]> invoicePdf(@PathVariable Long bookingId) {
        byte[] pdf = paymentService.getInvoicePdf(bookingId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-" + bookingId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @Operation(summary = "Get payment by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @bookingOwnership.canAccessPayment(#id)")
    public ResponseEntity<PaymentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }
}
