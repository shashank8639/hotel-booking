package com.hotelbooking.dto;

import com.hotelbooking.database.RoomStatus;
import com.hotelbooking.database.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Availability result for a single room over a requested date range.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomAvailabilityItem {

    private Long roomId;
    private String roomNumber;
    private RoomType roomType;
    private RoomStatus roomStatus;
    private BigDecimal effectivePrice;
    private boolean available;
    private String reason;
}
