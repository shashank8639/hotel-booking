package com.hotelbooking.dto;

import com.hotelbooking.database.PaymentMethod;
import com.hotelbooking.database.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    private Long id;
    private Long bookingId;
    private BigDecimal amount;
    private BigDecimal taxableAmount;
    private BigDecimal gstAmount;
    private BigDecimal refundedAmount;
    private String currency;
    private String baseCurrency;
    private BigDecimal fxRate;
    private BigDecimal amountInBase;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String transactionReference;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String invoiceNumber;
    private LocalDateTime invoiceGeneratedAt;
    private String failureReason;
    private LocalDateTime paidAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
