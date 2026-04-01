package com.hotelbooking.security;

import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.Guest;
import com.hotelbooking.entity.Payment;
import com.hotelbooking.repository.BookingRepository;
import com.hotelbooking.repository.GuestRepository;
import com.hotelbooking.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * SpEL bean for ownership checks, e.g. {@code @PreAuthorize("@bookingOwnership.canAccess(#id)")}.
 * <p>
 * Ownership key is guest email ↔ JWT subject (until a formal User↔Guest FK exists).
 * ADMIN bypasses ownership filters.
 */
@Component("bookingOwnership")
@RequiredArgsConstructor
public class BookingOwnership {

    private final BookingRepository bookingRepository;
    private final GuestRepository guestRepository;
    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public boolean canAccess(Long bookingId) {
        if (isAdmin()) {
            return true;
        }
        String email = currentEmailOrNull();
        if (email == null) {
            return false;
        }
        return bookingRepository.findById(bookingId)
                .map(Booking::getGuest)
                .map(guest -> emailMatches(guest, email))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean canAccessGuest(Long guestId) {
        if (isAdmin()) {
            return true;
        }
        String email = currentEmailOrNull();
        if (email == null) {
            return false;
        }
        return guestRepository.findById(guestId)
                .map(guest -> emailMatches(guest, email))
                .orElse(false);
    }

    /**
     * Customers may only look up their own email; admins may search any.
     */
    public boolean canSearchGuestEmail(String email) {
        if (isAdmin()) {
            return true;
        }
        String me = currentEmailOrNull();
        return me != null && email != null && me.equalsIgnoreCase(email.trim());
    }

    @Transactional(readOnly = true)
    public boolean canAccessPayment(Long paymentId) {
        if (isAdmin()) {
            return true;
        }
        String email = currentEmailOrNull();
        if (email == null) {
            return false;
        }
        return paymentRepository.findById(paymentId)
                .map(Payment::getBooking)
                .map(Booking::getGuest)
                .map(guest -> emailMatches(guest, email))
                .orElse(false);
    }

    public boolean canModifyStatus(Long bookingId) {
        return isAdmin();
    }

    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && hasRole(auth, "ROLE_ADMIN");
    }

    public String requireCurrentEmail() {
        String email = currentEmailOrNull();
        if (email == null) {
            throw new AccessDeniedException("Authentication required");
        }
        return email;
    }

    /**
     * Customers may only create/update guests whose email matches their login.
     */
    public void assertGuestEmailAllowed(String guestEmail) {
        if (isAdmin()) {
            return;
        }
        String me = requireCurrentEmail();
        if (guestEmail == null || !me.equalsIgnoreCase(guestEmail.trim())) {
            throw new AccessDeniedException("Guests must use your account email");
        }
    }

    public void assertCanAccessGuest(Long guestId) {
        if (!canAccessGuest(guestId)) {
            throw new AccessDeniedException("Not allowed to access this guest");
        }
    }

    public void assertCanAccessBooking(Long bookingId) {
        if (!canAccess(bookingId)) {
            throw new AccessDeniedException("Not allowed to access this booking");
        }
    }

    private String currentEmailOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            return null;
        }
        if ("anonymousUser".equals(auth.getName())) {
            return null;
        }
        return auth.getName();
    }

    private static boolean emailMatches(Guest guest, String email) {
        return guest.getEmail() != null && guest.getEmail().equalsIgnoreCase(email);
    }

    private boolean hasRole(Authentication auth, String role) {
        for (GrantedAuthority authority : auth.getAuthorities()) {
            if (role.equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}
