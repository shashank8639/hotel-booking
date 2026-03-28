package com.hotelbooking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Date-range overnight rate for a room (peak / off-season / event).
 * <p>
 * Resolution order for a stay night: matching seasonal row → {@code discountedPrice} → {@code pricePerNight}.
 * Overlapping seasons for the same room should be avoided at write time (admin responsibility).
 */
@Entity
@Table(name = "seasonal_room_prices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SeasonalRoomPrice extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    @ToString.Exclude
    private Room room;

    @Column(name = "season_label", length = 100)
    private String seasonLabel;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "price_per_night", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerNight;

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "INR";

    /** Inclusive start, exclusive end (hotel night model). */
    public boolean covers(LocalDate night) {
        return night != null
                && !night.isBefore(startDate)
                && night.isBefore(endDate);
    }
}
