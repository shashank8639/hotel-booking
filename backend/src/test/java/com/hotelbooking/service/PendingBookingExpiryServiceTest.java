package com.hotelbooking.service;

import com.hotelbooking.config.BookingProperties;
import com.hotelbooking.repository.BookingRepository;
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
class PendingBookingExpiryServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingProperties bookingProperties;

    @InjectMocks
    private PendingBookingExpiryService pendingBookingExpiryService;

    @Test
    void expirePendingHolds_shouldNoOpWhenDisabled() {
        when(bookingProperties.isExpiryEnabled()).thenReturn(false);

        pendingBookingExpiryService.expirePendingHolds();

        verify(bookingRepository, never()).cancelExpiredPendingHolds(any());
    }

    @Test
    void expirePendingHolds_shouldCancelExpired() {
        when(bookingProperties.isExpiryEnabled()).thenReturn(true);
        when(bookingRepository.cancelExpiredPendingHolds(any(LocalDateTime.class))).thenReturn(3);

        pendingBookingExpiryService.expirePendingHolds();

        verify(bookingRepository).cancelExpiredPendingHolds(any(LocalDateTime.class));
    }
}
