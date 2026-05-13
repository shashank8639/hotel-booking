package com.hotelbooking.util;

import com.hotelbooking.database.RoomType;
import com.hotelbooking.entity.Guest;
import com.hotelbooking.entity.Room;
import com.hotelbooking.entity.User;

import java.math.BigDecimal;

/**
 * Fluent builders for tests that need one-off field overrides.
 * Prefer {@link TestDataFactory} for common happy-path fixtures.
 */
public final class TestEntityBuilder {

    private TestEntityBuilder() {
    }

    public static GuestBuilder guest() {
        return new GuestBuilder();
    }

    public static RoomBuilder room() {
        return new RoomBuilder();
    }

    public static UserBuilder user() {
        return new UserBuilder();
    }

    public static final class GuestBuilder {
        private String firstName = "Asha";
        private String lastName = "Nair";
        private String email = "asha.nair@example.com";
        private String phone = "+91-9000000001";

        public GuestBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public GuestBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public GuestBuilder email(String email) {
            this.email = email;
            return this;
        }

        public GuestBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Guest build() {
            return Guest.builder()
                    .firstName(firstName)
                    .lastName(lastName)
                    .email(email)
                    .phone(phone)
                    .build();
        }
    }

    public static final class RoomBuilder {
        private String roomNumber = "101";
        private RoomType roomType = RoomType.STANDARD;
        private BigDecimal price = new BigDecimal("2500.00");

        public RoomBuilder roomNumber(String roomNumber) {
            this.roomNumber = roomNumber;
            return this;
        }

        public RoomBuilder type(RoomType roomType) {
            this.roomType = roomType;
            return this;
        }

        public RoomBuilder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public Room build() {
            return TestDataFactory.availableRoom(roomNumber, roomType, price);
        }
    }

    public static final class UserBuilder {
        private String email = "user@example.com";
        private boolean admin;

        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder asAdmin() {
            this.admin = true;
            return this;
        }

        public User build() {
            return admin ? TestDataFactory.adminUser(email) : TestDataFactory.customerUser(email);
        }
    }
}
