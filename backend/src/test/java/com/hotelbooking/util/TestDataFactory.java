package com.hotelbooking.util;

import com.hotelbooking.database.BookingStatus;
import com.hotelbooking.database.PaymentMethod;
import com.hotelbooking.database.PaymentStatus;
import com.hotelbooking.database.RoomStatus;
import com.hotelbooking.database.RoomType;
import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.Guest;
import com.hotelbooking.entity.Payment;
import com.hotelbooking.entity.Role;
import com.hotelbooking.entity.Room;
import com.hotelbooking.entity.User;
import com.hotelbooking.security.UserRole;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Central factory for realistic test entities.
 * <p>
 * Why: enterprise suites avoid copy-pasted builders in every test class.
 * Factories keep fixtures consistent and make intent obvious in interview demos.
 */
public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static Role role(UserRole name) {
        return Role.builder().name(name).build();
    }

    public static User customerUser(String email) {
        return User.builder()
                .email(email)
                .password("encoded-password")
                .firstName("Rahul")
                .lastName("Sharma")
                .enabled(true)
                .accountNonLocked(true)
                .roles(new HashSet<>(Set.of(role(UserRole.CUSTOMER))))
                .build();
    }

    public static User adminUser(String email) {
        return User.builder()
                .email(email)
                .password("encoded-password")
                .firstName("Admin")
                .lastName("User")
                .enabled(true)
                .accountNonLocked(true)
                .roles(new HashSet<>(Set.of(role(UserRole.ADMIN))))
                .build();
    }

    public static Guest guest(String email) {
        return Guest.builder()
                .firstName("Priya")
                .lastName("Patel")
                .email(email)
                .phone("+91-9876543210")
                .build();
    }

    public static Room availableRoom(String roomNumber, RoomType type, BigDecimal price) {
        return Room.builder()
                .roomNumber(roomNumber)
                .roomType(type)
                .floorNumber(2)
                .capacity(2)
                .pricePerNight(price)
                .status(RoomStatus.AVAILABLE)
                .description("Test room " + roomNumber)
                .build();
    }

    public static Booking pendingBooking(Guest guest, LocalDate checkIn, LocalDate checkOut, BigDecimal total) {
        return Booking.builder()
                .guest(guest)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .status(BookingStatus.PENDING)
                .totalAmount(total)
                .version(0L)
                .build();
    }

    public static Payment pendingPayment(Booking booking, BigDecimal amount) {
        return Payment.builder()
                .booking(booking)
                .amount(amount)
                .currency("INR")
                .paymentMethod(PaymentMethod.RAZORPAY)
                .status(PaymentStatus.PENDING)
                .transactionReference("txn-" + System.nanoTime())
                .build();
    }
}
