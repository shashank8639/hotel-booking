package com.hotelbooking.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelbooking.config.PaymentProperties;
import com.hotelbooking.config.RazorpayProperties;
import com.hotelbooking.database.BookingStatus;
import com.hotelbooking.database.PaymentMethod;
import com.hotelbooking.database.PaymentStatus;
import com.hotelbooking.dto.CreatePaymentOrderRequest;
import com.hotelbooking.dto.CreatePaymentOrderResponse;
import com.hotelbooking.dto.InvoiceResponse;
import com.hotelbooking.dto.PaymentResponse;
import com.hotelbooking.dto.RefundPaymentRequest;
import com.hotelbooking.dto.VerifyPaymentRequest;
import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.Guest;
import com.hotelbooking.entity.Payment;
import com.hotelbooking.entity.PaymentWebhookEvent;
import com.hotelbooking.exception.DuplicatePaymentException;
import com.hotelbooking.exception.InvalidPaymentSignatureException;
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
import com.hotelbooking.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private GuestRepository guestRepository;
    @Mock private PaymentWebhookEventRepository webhookEventRepository;
    @Mock private PaymentAttemptRepository paymentAttemptRepository;
    @Mock private PaymentMapper paymentMapper;
    @Mock private RazorpayGateway razorpayGateway;
    @Mock private RazorpayProperties razorpayProperties;
    @Mock private InvoiceService invoiceService;
    @Mock private PaymentReceiptService paymentReceiptService;
    @Mock private BookingOwnership bookingOwnership;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private PaymentProperties paymentProperties;
    private FxRateService fxRateService;
    private PaymentServiceImpl paymentService;

    private Booking booking;
    private Guest guest;

    @BeforeEach
    void setUp() {
        paymentProperties = new PaymentProperties();
        paymentProperties.setGstRate(new BigDecimal("0.18"));
        paymentProperties.setBaseCurrency("INR");
        paymentProperties.setPendingOrderMinutes(30);
        Map<String, BigDecimal> fx = new HashMap<>();
        fx.put("INR", BigDecimal.ONE);
        fx.put("USD", new BigDecimal("83.50"));
        paymentProperties.setFxRatesToBase(fx);
        fxRateService = new FxRateService(paymentProperties);

        paymentService = new PaymentServiceImpl(
                paymentRepository,
                bookingRepository,
                guestRepository,
                webhookEventRepository,
                paymentAttemptRepository,
                paymentMapper,
                razorpayGateway,
                razorpayProperties,
                paymentProperties,
                fxRateService,
                invoiceService,
                paymentReceiptService,
                objectMapper,
                bookingOwnership
        );

        lenient().when(paymentAttemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(bookingOwnership.isAdmin()).thenReturn(true);
        lenient().doNothing().when(bookingOwnership).assertCanAccessBooking(any());

        guest = Guest.builder().firstName("Asha").lastName("Patel").email("asha@example.com").build();
        guest.setId(1L);

        booking = Booking.builder()
                .guest(guest)
                .checkInDate(LocalDate.now().plusDays(5))
                .checkOutDate(LocalDate.now().plusDays(7))
                .status(BookingStatus.PENDING)
                .totalAmount(new BigDecimal("5000.00"))
                .build();
        booking.setId(10L);
    }

    @Test
    void createOrder_shouldCreateRazorpayOrderWithExclusiveGst() {
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(paymentRepository.existsByBookingIdAndStatus(10L, PaymentStatus.SUCCESS)).thenReturn(false);
        when(paymentRepository.findByBookingId(10L)).thenReturn(List.of());
        when(razorpayProperties.getCurrency()).thenReturn("INR");
        when(razorpayProperties.getKeyId()).thenReturn("rzp_test_key");
        when(razorpayGateway.createOrder(eq(new BigDecimal("5900.00")), eq("INR"), eq("booking_10")))
                .thenReturn(new RazorpayOrderResult("order_1", new BigDecimal("5900.00"), "INR", "booking_10", "created"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(100L);
            return p;
        });

        CreatePaymentOrderResponse response = paymentService.createOrder(
                CreatePaymentOrderRequest.builder().bookingId(10L).build()
        );

        assertThat(response.getRazorpayOrderId()).isEqualTo("order_1");
        assertThat(response.getAmount()).isEqualByComparingTo("5900.00");
        assertThat(response.getTaxableAmount()).isEqualByComparingTo("5000.00");
        assertThat(response.getGstAmount()).isEqualByComparingTo("900.00");
        assertThat(response.getRazorpayKeyId()).isEqualTo("rzp_test_key");
    }

    @Test
    void createOrder_shouldSnapshotFxForUsd() {
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(paymentRepository.existsByBookingIdAndStatus(10L, PaymentStatus.SUCCESS)).thenReturn(false);
        when(paymentRepository.findByBookingId(10L)).thenReturn(List.of());
        when(razorpayProperties.getKeyId()).thenReturn("rzp_test_key");
        when(razorpayGateway.createOrder(any(), eq("USD"), anyString()))
                .thenReturn(new RazorpayOrderResult("order_usd", new BigDecimal("5900.00"), "USD", "booking_10", "created"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(101L);
            return p;
        });

        CreatePaymentOrderResponse response = paymentService.createOrder(
                CreatePaymentOrderRequest.builder().bookingId(10L).currency("USD").build()
        );

        assertThat(response.getCurrency()).isEqualTo("USD");
        assertThat(response.getBaseCurrency()).isEqualTo("INR");
        assertThat(response.getFxRate()).isEqualByComparingTo("83.50");
        assertThat(response.getAmountInBase()).isEqualByComparingTo("492650.00"); // 5900 * 83.50
    }

    @Test
    void createOrder_shouldRejectWhenAlreadyPaid() {
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));
        when(paymentRepository.existsByBookingIdAndStatus(10L, PaymentStatus.SUCCESS)).thenReturn(true);

        assertThatThrownBy(() -> paymentService.createOrder(
                CreatePaymentOrderRequest.builder().bookingId(10L).build()
        )).isInstanceOf(DuplicatePaymentException.class);
    }

    @Test
    void createOrder_shouldRejectCancelledBooking() {
        booking.setStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> paymentService.createOrder(
                CreatePaymentOrderRequest.builder().bookingId(10L).build()
        )).isInstanceOf(PaymentValidationException.class);
    }

    @Test
    void verifyPayment_shouldMarkSuccessAndConfirmBooking() {
        Payment payment = Payment.builder()
                .booking(booking)
                .amount(new BigDecimal("5900.00"))
                .taxableAmount(new BigDecimal("5000.00"))
                .gstAmount(new BigDecimal("900.00"))
                .currency("INR")
                .paymentMethod(PaymentMethod.RAZORPAY)
                .status(PaymentStatus.PENDING)
                .razorpayOrderId("order_1")
                .build();
        payment.setId(100L);

        when(razorpayGateway.verifyPaymentSignature("order_1", "pay_1", "sig")).thenReturn(true);
        when(paymentRepository.findByRazorpayOrderId("order_1")).thenReturn(Optional.of(payment));
        when(paymentRepository.findByRazorpayPaymentId("pay_1")).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(invoiceService.buildInvoice(any())).thenReturn(InvoiceResponse.builder().invoiceNumber("INV-1").build());
        when(invoiceService.toPdf(any())).thenReturn(new byte[]{1, 2});
        when(paymentMapper.toResponse(any())).thenReturn(PaymentResponse.builder().id(100L).status(PaymentStatus.SUCCESS).build());

        PaymentResponse response = paymentService.verifyPayment(VerifyPaymentRequest.builder()
                .razorpayOrderId("order_1")
                .razorpayPaymentId("pay_1")
                .razorpaySignature("sig")
                .build());

        assertThat(response.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        verify(paymentRepository).stampPaidAtIfAbsent(eq(100L), any());
    }

    @Test
    void verifyPayment_shouldBeIdempotent() {
        Payment payment = Payment.builder()
                .booking(booking)
                .amount(new BigDecimal("5900.00"))
                .currency("INR")
                .paymentMethod(PaymentMethod.RAZORPAY)
                .status(PaymentStatus.SUCCESS)
                .razorpayOrderId("order_1")
                .razorpayPaymentId("pay_1")
                .build();
        payment.setId(100L);

        when(razorpayGateway.verifyPaymentSignature("order_1", "pay_1", "sig")).thenReturn(true);
        when(paymentRepository.findByRazorpayOrderId("order_1")).thenReturn(Optional.of(payment));
        when(paymentMapper.toResponse(payment)).thenReturn(
                PaymentResponse.builder().id(100L).status(PaymentStatus.SUCCESS).build()
        );

        PaymentResponse response = paymentService.verifyPayment(VerifyPaymentRequest.builder()
                .razorpayOrderId("order_1")
                .razorpayPaymentId("pay_1")
                .razorpaySignature("sig")
                .build());

        assertThat(response.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void verifyPayment_shouldRejectInvalidSignature() {
        when(razorpayGateway.verifyPaymentSignature(any(), any(), any())).thenReturn(false);

        assertThatThrownBy(() -> paymentService.verifyPayment(VerifyPaymentRequest.builder()
                .razorpayOrderId("order_1")
                .razorpayPaymentId("pay_1")
                .razorpaySignature("bad")
                .build()
        )).isInstanceOf(InvalidPaymentSignatureException.class);
    }

    @Test
    void refund_shouldProcessFullRefund() {
        Payment payment = Payment.builder()
                .booking(booking)
                .amount(new BigDecimal("5000.00"))
                .refundedAmount(BigDecimal.ZERO)
                .currency("INR")
                .paymentMethod(PaymentMethod.RAZORPAY)
                .status(PaymentStatus.SUCCESS)
                .razorpayPaymentId("pay_1")
                .build();
        payment.setId(100L);

        when(paymentRepository.findById(100L)).thenReturn(Optional.of(payment));
        when(razorpayGateway.refund("pay_1", new BigDecimal("5000.00"), "INR"))
                .thenReturn(new RazorpayRefundResult("rfnd_1", "pay_1", new BigDecimal("5000.00"), "processed"));
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(paymentMapper.toResponse(any())).thenReturn(
                PaymentResponse.builder().id(100L).status(PaymentStatus.REFUNDED).build()
        );

        PaymentResponse response = paymentService.refund(RefundPaymentRequest.builder().paymentId(100L).build());

        assertThat(response.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    @Test
    void refund_shouldRejectOverAmount() {
        Payment payment = Payment.builder()
                .booking(booking)
                .amount(new BigDecimal("5000.00"))
                .refundedAmount(BigDecimal.ZERO)
                .currency("INR")
                .paymentMethod(PaymentMethod.RAZORPAY)
                .status(PaymentStatus.SUCCESS)
                .razorpayPaymentId("pay_1")
                .build();
        payment.setId(100L);
        when(paymentRepository.findById(100L)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.refund(RefundPaymentRequest.builder()
                .paymentId(100L)
                .amount(new BigDecimal("6000.00"))
                .build()
        )).isInstanceOf(RefundValidationException.class);
    }

    @Test
    void refund_shouldRejectPendingPayment() {
        Payment payment = Payment.builder()
                .booking(booking)
                .amount(new BigDecimal("5000.00"))
                .currency("INR")
                .paymentMethod(PaymentMethod.RAZORPAY)
                .status(PaymentStatus.PENDING)
                .build();
        payment.setId(101L);
        when(paymentRepository.findById(101L)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.refund(RefundPaymentRequest.builder().paymentId(101L).build()))
                .isInstanceOf(RefundValidationException.class);
    }

    @Test
    void handleWebhook_shouldIgnoreReplay() {
        when(razorpayGateway.verifyWebhookSignature(any(), any())).thenReturn(true);
        when(webhookEventRepository.existsByEventId("evt_1")).thenReturn(true);

        paymentService.handleWebhook("{\"id\":\"evt_1\",\"event\":\"payment.captured\"}", "sig");

        verify(webhookEventRepository, never()).save(any());
    }

    @Test
    void handleWebhook_shouldAbsorbRetryStorm() {
        when(razorpayGateway.verifyWebhookSignature(any(), any())).thenReturn(true);
        when(webhookEventRepository.existsByEventId("evt_storm")).thenReturn(false);
        when(webhookEventRepository.save(any(PaymentWebhookEvent.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        paymentService.handleWebhook("{\"id\":\"evt_storm\",\"event\":\"payment.captured\"}", "sig");

        verify(paymentRepository, never()).findByRazorpayOrderId(any());
    }

    @Test
    void handleWebhook_shouldMarkCapturedPayment() {
        Payment payment = Payment.builder()
                .booking(booking)
                .amount(new BigDecimal("5900.00"))
                .taxableAmount(new BigDecimal("5000.00"))
                .gstAmount(new BigDecimal("900.00"))
                .currency("INR")
                .paymentMethod(PaymentMethod.RAZORPAY)
                .status(PaymentStatus.PENDING)
                .razorpayOrderId("order_9")
                .build();
        payment.setId(200L);

        String body = """
                {"id":"evt_2","event":"payment.captured","payload":{"payment":{"entity":{"id":"pay_9","order_id":"order_9"}}}}
                """;
        when(razorpayGateway.verifyWebhookSignature(eq(body), eq("sig"))).thenReturn(true);
        when(webhookEventRepository.existsByEventId("evt_2")).thenReturn(false);
        when(webhookEventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(paymentRepository.findByRazorpayOrderId("order_9")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(invoiceService.buildInvoice(any())).thenReturn(InvoiceResponse.builder().build());
        when(invoiceService.toPdf(any())).thenReturn(new byte[0]);

        paymentService.handleWebhook(body, "sig");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        ArgumentCaptor<PaymentWebhookEvent> captor = ArgumentCaptor.forClass(PaymentWebhookEvent.class);
        verify(webhookEventRepository).save(captor.capture());
        assertThat(captor.getValue().getEventId()).isEqualTo("evt_2");
    }
}
