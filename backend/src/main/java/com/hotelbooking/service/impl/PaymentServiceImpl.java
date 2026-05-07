package com.hotelbooking.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelbooking.config.PaymentProperties;
import com.hotelbooking.config.RazorpayProperties;
import com.hotelbooking.database.BookingStatus;
import com.hotelbooking.database.PaymentAttemptType;
import com.hotelbooking.database.PaymentMethod;
import com.hotelbooking.database.PaymentStatus;
import com.hotelbooking.dto.CreatePaymentOrderRequest;
import com.hotelbooking.dto.CreatePaymentOrderResponse;
import com.hotelbooking.dto.InvoiceResponse;
import com.hotelbooking.dto.PaymentResponse;
import com.hotelbooking.dto.RefundPaymentRequest;
import com.hotelbooking.dto.VerifyPaymentRequest;
import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.Payment;
import com.hotelbooking.entity.PaymentAttempt;
import com.hotelbooking.entity.PaymentWebhookEvent;
import com.hotelbooking.exception.BookingNotFoundException;
import com.hotelbooking.exception.DuplicatePaymentException;
import com.hotelbooking.exception.InvalidPaymentSignatureException;
import com.hotelbooking.exception.PaymentNotFoundException;
import com.hotelbooking.exception.PaymentValidationException;
import com.hotelbooking.exception.RefundValidationException;
import com.hotelbooking.mapper.PaymentMapper;
import com.hotelbooking.payment.RazorpayGateway;
import com.hotelbooking.payment.RazorpayOrderResult;
import com.hotelbooking.payment.RazorpayRefundResult;
import com.hotelbooking.repository.BookingRepository;
import com.hotelbooking.repository.GuestRepository;
import com.hotelbooking.repository.PaymentAttemptRepository;
import com.hotelbooking.repository.PaymentRepository;
import com.hotelbooking.repository.PaymentWebhookEventRepository;
import com.hotelbooking.security.BookingOwnership;
import com.hotelbooking.service.FxRateService;
import com.hotelbooking.service.InvoiceService;
import com.hotelbooking.service.PaymentReceiptService;
import com.hotelbooking.service.PaymentService;
import com.hotelbooking.util.InvoiceNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private static final Set<BookingStatus> PAYABLE_STATUSES = Set.of(
            BookingStatus.PENDING,
            BookingStatus.CONFIRMED
    );

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final GuestRepository guestRepository;
    private final PaymentWebhookEventRepository webhookEventRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final PaymentMapper paymentMapper;
    private final RazorpayGateway razorpayGateway;
    private final RazorpayProperties razorpayProperties;
    private final PaymentProperties paymentProperties;
    private final FxRateService fxRateService;
    private final InvoiceService invoiceService;
    private final PaymentReceiptService paymentReceiptService;
    private final ObjectMapper objectMapper;
    private final BookingOwnership bookingOwnership;

    @Override
    @Transactional
    public CreatePaymentOrderResponse createOrder(CreatePaymentOrderRequest request) {
        log.info("Payment order requested for bookingId={}", request.getBookingId());

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new BookingNotFoundException(request.getBookingId()));

        try {
            assertBookingPayable(booking);

            if (paymentRepository.existsByBookingIdAndStatus(booking.getId(), PaymentStatus.SUCCESS)) {
                throw new DuplicatePaymentException(
                        "Booking already has a successful payment: " + booking.getId()
                );
            }

            Payment existingPending = paymentRepository.findByBookingId(booking.getId()).stream()
                    .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                    .filter(p -> p.getExpiresAt() == null || p.getExpiresAt().isAfter(LocalDateTime.now()))
                    .findFirst()
                    .orElse(null);
            if (existingPending != null && existingPending.getRazorpayOrderId() != null) {
                log.info("Reusing pending payment id={} orderId={}",
                        existingPending.getId(), existingPending.getRazorpayOrderId());
                recordAttempt(existingPending, booking.getId(), PaymentAttemptType.CREATE_ORDER, true,
                        "reuse-pending", existingPending.getRazorpayOrderId());
                return toOrderResponse(existingPending);
            }

            BigDecimal taxable = booking.getTotalAmount();
            if (taxable == null || taxable.compareTo(BigDecimal.ZERO) <= 0) {
                throw new PaymentValidationException("Booking total amount must be greater than zero");
            }

            BigDecimal gstRate = paymentProperties.getGstRate();
            BigDecimal gstAmount = taxable.multiply(gstRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal chargeAmount = taxable.add(gstAmount);

            String currency = StringUtils.hasText(request.getCurrency())
                    ? request.getCurrency().trim().toUpperCase()
                    : (StringUtils.hasText(razorpayProperties.getCurrency())
                    ? razorpayProperties.getCurrency()
                    : fxRateService.baseCurrency());

            BigDecimal fxRate = fxRateService.rateToBase(currency);
            BigDecimal amountInBase = fxRateService.toBase(chargeAmount, currency);
            String baseCurrency = fxRateService.baseCurrency();

            String receipt = "booking_" + booking.getId();
            RazorpayOrderResult order = razorpayGateway.createOrder(chargeAmount, currency, receipt);

            Payment payment = Payment.builder()
                    .booking(booking)
                    .amount(chargeAmount)
                    .taxableAmount(taxable)
                    .gstAmount(gstAmount)
                    .refundedAmount(BigDecimal.ZERO)
                    .currency(currency)
                    .baseCurrency(baseCurrency)
                    .fxRate(fxRate)
                    .amountInBase(amountInBase)
                    .paymentMethod(PaymentMethod.RAZORPAY)
                    .status(PaymentStatus.PENDING)
                    .razorpayOrderId(order.orderId())
                    .transactionReference(order.orderId())
                    .expiresAt(LocalDateTime.now().plusMinutes(paymentProperties.getPendingOrderMinutes()))
                    .build();

            Payment saved = paymentRepository.save(payment);
            recordAttempt(saved, booking.getId(), PaymentAttemptType.CREATE_ORDER, true,
                    "created taxable=" + taxable + " gst=" + gstAmount + " fx=" + fxRate,
                    order.orderId());
            log.info("Razorpay order created paymentId={}, orderId={}, charge={}, currency={}, fxRate={}",
                    saved.getId(), order.orderId(), chargeAmount, currency, fxRate);
            return toOrderResponse(saved);
        } catch (RuntimeException ex) {
            recordAttempt(null, booking.getId(), PaymentAttemptType.CREATE_ORDER, false,
                    truncate(ex.getMessage()), "booking-" + booking.getId());
            throw ex;
        }
    }

    @Override
    @Transactional
    public PaymentResponse verifyPayment(VerifyPaymentRequest request) {
        log.info("Verifying payment orderId={}, paymentId={}",
                request.getRazorpayOrderId(), request.getRazorpayPaymentId());

        String fingerprint = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();

        if (!razorpayGateway.verifyPaymentSignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        )) {
            log.warn("Invalid payment signature for orderId={}", request.getRazorpayOrderId());
            recordAttempt(null, null, PaymentAttemptType.VERIFY, false, "invalid-signature", fingerprint);
            throw new InvalidPaymentSignatureException();
        }

        Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found for order: " + request.getRazorpayOrderId()));

        bookingOwnership.assertCanAccessBooking(payment.getBooking().getId());

        // Idempotent verify: same SUCCESS payment → return as-is
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            if (request.getRazorpayPaymentId().equals(payment.getRazorpayPaymentId())) {
                recordAttempt(payment, payment.getBooking().getId(), PaymentAttemptType.VERIFY, true,
                        "idempotent-success", fingerprint);
                return paymentMapper.toResponse(payment);
            }
            recordAttempt(payment, payment.getBooking().getId(), PaymentAttemptType.VERIFY, false,
                    "already-completed-mismatch", fingerprint);
            throw new DuplicatePaymentException("Payment already completed for this order");
        }

        if (payment.getStatus() == PaymentStatus.CANCELLED) {
            recordAttempt(payment, payment.getBooking().getId(), PaymentAttemptType.VERIFY, false,
                    "order-expired", fingerprint);
            throw new PaymentValidationException("Payment order has expired");
        }

        assertBookingPayable(payment.getBooking());

        if (paymentRepository.findByRazorpayPaymentId(request.getRazorpayPaymentId()).isPresent()) {
            recordAttempt(payment, payment.getBooking().getId(), PaymentAttemptType.VERIFY, false,
                    "duplicate-razorpay-payment-id", fingerprint);
            throw new DuplicatePaymentException("Razorpay payment ID already recorded");
        }

        markPaymentSuccess(payment, request.getRazorpayPaymentId());
        recordAttempt(payment, payment.getBooking().getId(), PaymentAttemptType.VERIFY, true,
                "verified", fingerprint);
        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse refund(RefundPaymentRequest request) {
        log.info("Refund requested paymentId={}, amount={}", request.getPaymentId(), request.getAmount());

        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new PaymentNotFoundException(request.getPaymentId()));

        if (payment.getStatus() != PaymentStatus.SUCCESS && payment.getStatus() != PaymentStatus.REFUNDED) {
            throw new RefundValidationException("Only successful payments can be refunded");
        }
        if (payment.getRazorpayPaymentId() == null) {
            throw new RefundValidationException("Missing Razorpay payment ID for refund");
        }

        BigDecimal refundable = payment.getRefundableAmount();
        BigDecimal refundAmount = request.getAmount() != null ? request.getAmount() : refundable;

        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0 || refundAmount.compareTo(refundable) > 0) {
            throw new RefundValidationException(
                    "Refund amount must be between 0.01 and " + refundable
            );
        }

        RazorpayRefundResult result = razorpayGateway.refund(
                payment.getRazorpayPaymentId(),
                refundAmount,
                payment.getCurrency()
        );

        payment.setRefundedAmount(payment.getRefundedAmount().add(refundAmount));
        if (payment.getRefundableAmount().compareTo(BigDecimal.ZERO) == 0) {
            payment.setStatus(PaymentStatus.REFUNDED);
        }
        payment.setTransactionReference(result.refundId());

        Payment saved = paymentRepository.save(payment);
        recordAttempt(saved, saved.getBooking().getId(), PaymentAttemptType.REFUND, true,
                "refunded=" + refundAmount + " id=" + result.refundId(), result.refundId());
        log.info("Refund processed paymentId={}, refundId={}, refundedTotal={}",
                saved.getId(), result.refundId(), saved.getRefundedAmount());
        return paymentMapper.toResponse(saved);
    }

    @Override
    public PaymentResponse getPaymentById(Long id) {
        return paymentMapper.toResponse(findOrThrow(id));
    }

    @Override
    public Page<PaymentResponse> getPaymentHistory(
            Long bookingId,
            Long guestId,
            PaymentStatus status,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable
    ) {
        if (!bookingOwnership.isAdmin()) {
            // Customers may only see payments for their own guest email / bookings.
            if (bookingId != null) {
                bookingOwnership.assertCanAccessBooking(bookingId);
                return paymentRepository.findByBookingId(bookingId, pageable).map(paymentMapper::toResponse);
            }
            Long ownGuestId = resolveOwnGuestIdOrNull();
            if (ownGuestId == null) {
                return Page.empty(pageable);
            }
            if (guestId != null && !ownGuestId.equals(guestId)) {
                throw new AccessDeniedException("Not allowed to view another guest's payments");
            }
            return paymentRepository.findByGuestId(ownGuestId, pageable).map(paymentMapper::toResponse);
        }

        if (bookingId != null) {
            return paymentRepository.findByBookingId(bookingId, pageable).map(paymentMapper::toResponse);
        }
        if (guestId != null) {
            return paymentRepository.findByGuestId(guestId, pageable).map(paymentMapper::toResponse);
        }
        if (status != null) {
            return paymentRepository.findByStatus(status, pageable).map(paymentMapper::toResponse);
        }
        if (fromDate != null && toDate != null) {
            LocalDateTime from = fromDate.atStartOfDay();
            LocalDateTime to = toDate.plusDays(1).atStartOfDay();
            return paymentRepository.findByCreatedAtBetween(from, to, pageable).map(paymentMapper::toResponse);
        }
        return paymentRepository.findAllByOrderByCreatedAtDesc(pageable).map(paymentMapper::toResponse);
    }

    @Override
    @Transactional
    public void handleWebhook(String rawBody, String signatureHeader) {
        log.info("Webhook received ({} bytes)", rawBody != null ? rawBody.length() : 0);

        if (!razorpayGateway.verifyWebhookSignature(rawBody, signatureHeader)) {
            log.warn("Webhook signature verification failed");
            recordAttempt(null, null, PaymentAttemptType.WEBHOOK, false, "invalid-signature", null);
            throw new InvalidPaymentSignatureException();
        }

        try {
            JsonNode root = objectMapper.readTree(rawBody);
            final String eventId = root.hasNonNull("id")
                    ? root.get("id").asText()
                    : text(root, "event") + ":" + text(root.path("payload").path("payment").path("entity"), "id");
            String eventType = text(root, "event");

            if (webhookEventRepository.existsByEventId(eventId)) {
                log.info("Ignoring replayed webhook eventId={}", eventId);
                recordAttempt(null, null, PaymentAttemptType.WEBHOOK, true, "replay-ignored", eventId);
                return;
            }

            try {
                webhookEventRepository.save(PaymentWebhookEvent.builder()
                        .eventId(eventId)
                        .eventType(eventType)
                        .payloadHash(sha256(rawBody))
                        .build());
            } catch (DataIntegrityViolationException race) {
                // Retry storm: concurrent inserts race on uk_payment_webhook_event_id
                log.info("Webhook retry storm absorbed for eventId={} ({})", eventId, race.getClass().getSimpleName());
                recordAttempt(null, null, PaymentAttemptType.WEBHOOK, true, "retry-storm-absorbed", eventId);
                return;
            }

            JsonNode paymentEntity = root.path("payload").path("payment").path("entity");
            String orderId = text(paymentEntity, "order_id");
            String paymentId = text(paymentEntity, "id");

            switch (eventType) {
                case "payment.authorized", "payment.captured" -> {
                    paymentRepository.findByRazorpayOrderId(orderId).ifPresent(payment -> {
                        if (payment.getStatus() != PaymentStatus.SUCCESS) {
                            markPaymentSuccess(payment, paymentId);
                            recordAttempt(payment, payment.getBooking().getId(), PaymentAttemptType.WEBHOOK, true,
                                    eventType, eventId);
                        } else {
                            recordAttempt(payment, payment.getBooking().getId(), PaymentAttemptType.WEBHOOK, true,
                                    "idempotent-" + eventType, eventId);
                        }
                    });
                }
                case "payment.failed" -> {
                    paymentRepository.findByRazorpayOrderId(orderId).ifPresent(payment -> {
                        if (payment.getStatus() == PaymentStatus.PENDING) {
                            payment.setStatus(PaymentStatus.FAILED);
                            payment.setFailureReason(text(paymentEntity, "error_description"));
                            paymentRepository.save(payment);
                            recordAttempt(payment, payment.getBooking().getId(), PaymentAttemptType.WEBHOOK, true,
                                    "failed", eventId);
                            log.info("Payment marked FAILED via webhook paymentId={}", payment.getId());
                        }
                    });
                }
                case "refund.created", "refund.processed" -> {
                    JsonNode refundEntity = root.path("payload").path("refund").path("entity");
                    String rpPaymentId = text(refundEntity, "payment_id");
                    String refundId = text(refundEntity, "id");
                    paymentRepository.findByRazorpayPaymentId(rpPaymentId).ifPresent(payment -> {
                        // Idempotent under retry storm: skip if this refund id already applied as txn ref
                        if (refundId != null && refundId.equals(payment.getTransactionReference())) {
                            recordAttempt(payment, payment.getBooking().getId(), PaymentAttemptType.WEBHOOK, true,
                                    "refund-replay", eventId);
                            return;
                        }
                        long amountPaise = refundEntity.path("amount").asLong(0);
                        BigDecimal refundMajor = BigDecimal.valueOf(amountPaise, 2);
                        if (refundMajor.compareTo(BigDecimal.ZERO) > 0
                                && payment.getRefundedAmount().compareTo(payment.getAmount()) < 0) {
                            BigDecimal newRefunded = payment.getRefundedAmount().add(refundMajor);
                            if (newRefunded.compareTo(payment.getAmount()) > 0) {
                                newRefunded = payment.getAmount();
                            }
                            payment.setRefundedAmount(newRefunded);
                            if (payment.getRefundableAmount().compareTo(BigDecimal.ZERO) == 0) {
                                payment.setStatus(PaymentStatus.REFUNDED);
                            }
                            if (refundId != null) {
                                payment.setTransactionReference(refundId);
                            }
                            paymentRepository.save(payment);
                            recordAttempt(payment, payment.getBooking().getId(), PaymentAttemptType.WEBHOOK, true,
                                    "refund=" + refundMajor, eventId);
                            log.info("Refund webhook applied paymentId={}, refunded={}",
                                    payment.getId(), payment.getRefundedAmount());
                        }
                    });
                }
                default -> log.debug("Unhandled webhook eventType={}", eventType);
            }
        } catch (InvalidPaymentSignatureException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Webhook processing failed: {}", ex.getMessage(), ex);
            recordAttempt(null, null, PaymentAttemptType.WEBHOOK, false, truncate(ex.getMessage()), null);
            throw new PaymentValidationException("Unable to process webhook payload");
        }
    }

    @Override
    public InvoiceResponse getInvoice(Long bookingId) {
        return invoiceService.getInvoiceForBooking(bookingId);
    }

    @Override
    public byte[] getInvoicePdf(Long bookingId) {
        InvoiceResponse invoice = invoiceService.getInvoiceForBooking(bookingId);
        return invoiceService.toPdf(invoice);
    }

    private void markPaymentSuccess(Payment payment, String razorpayPaymentId) {
        LocalDateTime paidAt = LocalDateTime.now();
        payment.setRazorpayPaymentId(razorpayPaymentId);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(paidAt);
        payment.setTransactionReference(razorpayPaymentId);
        payment.setExpiresAt(null);

        if (payment.getInvoiceNumber() == null) {
            payment.setInvoiceNumber(InvoiceNumberGenerator.next());
            payment.setInvoiceGeneratedAt(LocalDateTime.now());
        }

        Payment saved = paymentRepository.save(payment);
        // Confirm + invoice while the persistence context still holds the booking proxy.
        // stampPaidAtIfAbsent uses clearAutomatically=true and would otherwise trigger
        // LazyInitializationException on booking/guest/rooms (open-in-view is false).
        Long bookingId = saved.getBooking().getId();
        confirmBookingIfPending(saved.getBooking());

        InvoiceResponse invoice = invoiceService.buildInvoice(saved);
        byte[] pdf = invoiceService.toPdf(invoice);
        paymentReceiptService.sendPaymentReceiptAsync(saved, invoice, pdf);

        paymentRepository.stampPaidAtIfAbsent(saved.getId(), paidAt);

        log.info("Payment SUCCESS id={}, invoice={}, bookingId={}",
                saved.getId(), saved.getInvoiceNumber(), bookingId);
    }

    private void confirmBookingIfPending(Booking booking) {
        if (booking.getStatus() == BookingStatus.PENDING) {
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setHoldExpiresAt(null);
            bookingRepository.save(booking);
            log.info("Booking {} confirmed after successful payment", booking.getId());
        }
    }

    private void assertBookingPayable(Booking booking) {
        if (!PAYABLE_STATUSES.contains(booking.getStatus())) {
            throw new PaymentValidationException(
                    "Booking status " + booking.getStatus() + " is not payable"
            );
        }
        if (booking.getStatus() == BookingStatus.PENDING
                && booking.getHoldExpiresAt() != null
                && booking.getHoldExpiresAt().isBefore(LocalDateTime.now())) {
            throw new PaymentValidationException("Booking hold has expired; create a new booking to pay");
        }
    }

    private Long resolveOwnGuestIdOrNull() {
        String email = bookingOwnership.requireCurrentEmail();
        return guestRepository.findByEmailIgnoreCase(email).map(g -> g.getId()).orElse(null);
    }

    private void recordAttempt(
            Payment payment,
            Long bookingId,
            PaymentAttemptType type,
            boolean success,
            String detail,
            String fingerprint
    ) {
        try {
            paymentAttemptRepository.save(PaymentAttempt.builder()
                    .payment(payment)
                    .bookingId(bookingId)
                    .attemptType(type)
                    .success(success)
                    .detail(truncate(detail))
                    .requestFingerprint(fingerprint)
                    .build());
        } catch (Exception ex) {
            log.warn("Failed to persist payment attempt audit: {}", ex.getMessage());
        }
    }

    private CreatePaymentOrderResponse toOrderResponse(Payment payment) {
        String keyId = razorpayProperties.getKeyId();
        if (keyId == null || keyId.isBlank()) {
            keyId = "rzp_test_mock_key";
        }
        return CreatePaymentOrderResponse.builder()
                .paymentId(payment.getId())
                .bookingId(payment.getBooking().getId())
                .razorpayOrderId(payment.getRazorpayOrderId())
                .razorpayKeyId(keyId)
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .baseCurrency(payment.getBaseCurrency())
                .fxRate(payment.getFxRate())
                .amountInBase(payment.getAmountInBase())
                .taxableAmount(payment.getTaxableAmount())
                .gstAmount(payment.getGstAmount())
                .status(payment.getStatus().name())
                .build();
    }

    private Payment findOrThrow(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private static String sha256(String body) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(body.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to hash webhook payload", ex);
        }
    }

    private static String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= 500 ? value : value.substring(0, 500);
    }
}
