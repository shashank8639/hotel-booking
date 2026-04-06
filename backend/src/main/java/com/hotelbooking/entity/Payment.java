package com.hotelbooking.entity;

import com.hotelbooking.database.PaymentMethod;
import com.hotelbooking.database.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a monetary transaction against a booking (Razorpay or offline).
 */
@Entity
@Table(
        name = "payments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payments_transaction_reference", columnNames = "transaction_reference"),
                @UniqueConstraint(name = "uk_payments_invoice_number", columnNames = "invoice_number")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true)
public class Payment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    @ToString.Exclude
    private Booking booking;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /** Net / taxable amount before GST (exclusive model). */
    @Column(name = "taxable_amount", precision = 12, scale = 2)
    private BigDecimal taxableAmount;

    /** GST charged on {@link #taxableAmount}. */
    @Column(name = "gst_amount", precision = 12, scale = 2)
    private BigDecimal gstAmount;

    @Column(name = "refunded_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal refundedAmount = BigDecimal.ZERO;

    /** Checkout / charge currency (may differ from base after FX). */
    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "INR";

    /** Hotel books currency at order time. */
    @Column(name = "base_currency", nullable = false, length = 3)
    @Builder.Default
    private String baseCurrency = "INR";

    /** Snapshot: 1 unit of {@link #currency} → units of {@link #baseCurrency}. */
    @Column(name = "fx_rate", precision = 18, scale = 8)
    private BigDecimal fxRate;

    /** {@link #amount} converted to {@link #baseCurrency} using {@link #fxRate}. */
    @Column(name = "amount_in_base", precision = 12, scale = 2)
    private BigDecimal amountInBase;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 50)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @EqualsAndHashCode.Include
    @Column(name = "transaction_reference", length = 100)
    private String transactionReference;

    @Column(name = "razorpay_order_id", length = 100)
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id", length = 100)
    private String razorpayPaymentId;

    @Column(name = "invoice_number", length = 50)
    private String invoiceNumber;

    @Column(name = "invoice_generated_at")
    private LocalDateTime invoiceGeneratedAt;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    /**
     * Set once when payment succeeds. {@code updatable = false} prevents accidental overwrites
     * on later {@code save()} calls; stamp via {@code PaymentRepository.stampPaidAtIfAbsent}.
     */
    @Column(name = "paid_at", updatable = false)
    private LocalDateTime paidAt;

    /** Soft-hold deadline for PENDING gateway orders. */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    public BigDecimal getRefundableAmount() {
        return amount.subtract(refundedAmount == null ? BigDecimal.ZERO : refundedAmount);
    }
}
