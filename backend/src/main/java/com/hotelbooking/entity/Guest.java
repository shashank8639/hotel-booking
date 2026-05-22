package com.hotelbooking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a hotel guest who can create one or more bookings.
 */
@Entity
@Table(
        name = "guests",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_guests_email", columnNames = "email")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true)
public class Guest extends BaseEntity {

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @EqualsAndHashCode.Include
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * Inverse side of Guest ↔ Booking.
     * No cascade: bookings are managed from the Booking owning side.
     */
    @OneToMany(mappedBy = "guest", fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private List<Booking> bookings = new ArrayList<>();
}
