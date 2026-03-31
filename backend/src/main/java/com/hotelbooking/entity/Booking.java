package com.hotelbooking.entity;

import com.hotelbooking.database.BookingStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a reservation made by a guest for one or more rooms.
 * <p>
 * {@link SQLRestriction} hides {@code CANCELLED} rows from normal entity queries (active inventory view).
 * Use native queries when cancelled history must be loaded (reports / admin status filter).
 * Overlap checks also exclude {@code CANCELLED} explicitly in repository JPQL.
 * {@link Version} enables optimistic locking when two agents update the same booking.
 */
@Entity
@Table(name = "bookings",
        indexes = {
                @Index(name = "idx_booking_guest_status", columnList = "guest_id,status"),
                @Index(name = "idx_bookings_status_dates", columnList = "status,check_in_date,check_out_date"),
                @Index(name = "idx_bookings_pending_hold", columnList = "status,hold_expires_at"),
                @Index(name = "idx_bookings_guest_status_checkin", columnList = "guest_id,status,check_in_date")
        })
@SQLRestriction("status <> 'CANCELLED'")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Booking extends BaseEntity {

    /**
     * Owning side of Guest ↔ Booking.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guest_id", nullable = false)
    @ToString.Exclude
    private Guest guest;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;

    /**
     * Soft-hold deadline for {@link BookingStatus#PENDING}. After this instant a scheduled job
     * auto-cancels the booking so inventory is released.
     */
    @Column(name = "hold_expires_at")
    private LocalDateTime holdExpiresAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    /**
     * Owning side of Booking ↔ BookingRoom aggregate.
     * ALL + orphanRemoval: line items belong exclusively to this booking.
     */
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private List<BookingRoom> bookingRooms = new ArrayList<>();

    /**
     * Owning side of Booking ↔ Payment.
     * ALL without orphanRemoval: payment records are retained for audit.
     */
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private List<Payment> payments = new ArrayList<>();

    public void addBookingRoom(BookingRoom bookingRoom) {
        bookingRooms.add(bookingRoom);
        bookingRoom.setBooking(this);
    }

    public void removeBookingRoom(BookingRoom bookingRoom) {
        bookingRooms.remove(bookingRoom);
        bookingRoom.setBooking(null);
    }

    public void addPayment(Payment payment) {
        payments.add(payment);
        payment.setBooking(this);
    }
}
