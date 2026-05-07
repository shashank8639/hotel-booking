package com.hotelbooking.service;

import com.hotelbooking.config.PaymentProperties;
import com.hotelbooking.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PendingPaymentExpiryServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentProperties paymentProperties;

    @InjectMocks
    private PendingPaymentExpiryService pendingPaymentExpiryService;

    @Test
    void expirePendingOrders_shouldNoOpWhenDisabled() {
        when(paymentProperties.isExpiryEnabled()).thenReturn(false);

        pendingPaymentExpiryService.expirePendingOrders();

        verify(paymentRepository, never()).cancelExpiredPendingOrders(any());
    }

    @Test
    void expirePendingOrders_shouldCancelExpired() {
        when(paymentProperties.isExpiryEnabled()).thenReturn(true);
        when(paymentRepository.cancelExpiredPendingOrders(any(LocalDateTime.class))).thenReturn(2);

        pendingPaymentExpiryService.expirePendingOrders();

        verify(paymentRepository).cancelExpiredPendingOrders(any(LocalDateTime.class));
    }
}
