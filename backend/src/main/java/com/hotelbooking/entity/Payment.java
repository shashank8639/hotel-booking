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
 * Represents a monetary transaction against a booking.
 */
@Entity
@Table(
        name = "payments",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payments_transaction_reference", columnNames = "transaction_reference")
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

    /**
     * Owning side of Booking ↔ Payment.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    @ToString.Exclude
    private Booking booking;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

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

    @Column(name = "paid_at",updatable = false)
    private LocalDateTime paidAt;
}
