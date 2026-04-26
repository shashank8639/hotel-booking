package com.hotelbooking.dto;

import com.hotelbooking.database.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Full booking confirmation returned to clients.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {

    private Long id;
    private Long guestId;
    private String guestFirstName;
    private String guestLastName;
    private String guestEmail;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer numberOfNights;
    private BookingStatus status;
    private BigDecimal totalAmount;
    private String specialRequests;
    private LocalDateTime holdExpiresAt;
    private List<BookingRoomResponse> rooms;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
