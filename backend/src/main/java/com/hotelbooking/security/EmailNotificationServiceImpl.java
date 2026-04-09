package com.hotelbooking.security;

import com.hotelbooking.notification.AsyncNotificationFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Password-reset emails now route through the Module 8 notification stack.
 */
@Service
@RequiredArgsConstructor
public class EmailNotificationServiceImpl implements EmailNotificationService {

    private final AsyncNotificationFacade asyncNotificationFacade;

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        asyncNotificationFacade.passwordResetAsync(toEmail, resetToken);
    }
}
