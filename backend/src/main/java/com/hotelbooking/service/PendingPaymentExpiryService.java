package com.hotelbooking.service;

import com.hotelbooking.config.PaymentProperties;
import com.hotelbooking.database.PaymentStatus;
import com.hotelbooking.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Cancels unpaid PENDING payment orders after soft-hold expiry so gateway carts cannot linger.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PendingPaymentExpiryService {

    private final PaymentRepository paymentRepository;
    private final PaymentProperties paymentProperties;

    @Scheduled(fixedDelayString = "${app.payment.expiry-fixed-delay-ms:300000}")
    @Transactional
    public void expirePendingOrders() {
        if (!paymentProperties.isExpiryEnabled()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        int cancelled = paymentRepository.cancelExpiredPendingOrders(now);
        if (cancelled > 0) {
            log.info("Payment soft-hold expiry: cancelled {} PENDING order(s) with expires_at < {}", cancelled, now);
        } else {
            log.debug("Payment soft-hold expiry sweep: none expired at {}", now);
        }
    }
}
