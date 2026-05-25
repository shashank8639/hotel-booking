package com.hotelbooking.repository;

import com.hotelbooking.database.PaymentStatus;
import com.hotelbooking.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByBookingId(Long bookingId);

    Page<Payment> findByBookingId(Long bookingId, Pageable pageable);

    List<Payment> findByStatus(PaymentStatus status);

    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);

    Optional<Payment> findByTransactionReference(String transactionReference);

    boolean existsByTransactionReference(String transactionReference);

    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);

    boolean existsByBookingIdAndStatus(Long bookingId, PaymentStatus status);

    Optional<Payment> findFirstByBookingIdAndStatusOrderByPaidAtDesc(Long bookingId, PaymentStatus status);

    @Query("""
            SELECT p FROM Payment p
            WHERE p.booking.guest.id = :guestId
            ORDER BY p.createdAt DESC
            """)
    Page<Payment> findByGuestId(@Param("guestId") Long guestId, Pageable pageable);

    @Query("""
            SELECT p FROM Payment p
            WHERE p.createdAt >= :from AND p.createdAt < :to
            ORDER BY p.createdAt DESC
            """)
    Page<Payment> findByCreatedAtBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );

    Page<Payment> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Stamps {@code paid_at} once. Needed because {@code Payment.paidAt} is {@code updatable = false},
     * so a normal entity {@code save()} after insert will not write a later paid timestamp.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE payments
            SET paid_at = :paidAt
            WHERE id = :id AND paid_at IS NULL
            """, nativeQuery = true)
    int stampPaidAtIfAbsent(@Param("id") Long id, @Param("paidAt") LocalDateTime paidAt);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE payments
            SET status = 'CANCELLED', updated_at = CURRENT_TIMESTAMP, failure_reason = 'PENDING order expired'
            WHERE status = 'PENDING'
              AND expires_at IS NOT NULL
              AND expires_at < :now
            """, nativeQuery = true)
    int cancelExpiredPendingOrders(@Param("now") LocalDateTime now);
}
