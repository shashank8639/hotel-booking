package com.hotelbooking.entity;

import com.hotelbooking.database.PaymentAttemptType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Append-only audit row for payment operations (create / verify / webhook / refund / expire).
 */
@Entity
@Table(name = "payment_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PaymentAttempt extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    @ToString.Exclude
    private Payment payment;

    @Column(name = "booking_id")
    private Long bookingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "attempt_type", nullable = false, length = 40)
    private PaymentAttemptType attemptType;

    @Column(name = "success", nullable = false)
    private boolean success;

    @Column(name = "detail", length = 500)
    private String detail;

    @Column(name = "request_fingerprint", length = 128)
    private String requestFingerprint;
}
