package com.hotelbooking.repository;

import com.hotelbooking.entity.PaymentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, Long> {

    List<PaymentAttempt> findByPaymentIdOrderByCreatedAtDesc(Long paymentId);

    List<PaymentAttempt> findByBookingIdOrderByCreatedAtDesc(Long bookingId);
}
