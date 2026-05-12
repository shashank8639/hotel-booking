package com.hotelbooking.dto;

import com.hotelbooking.database.BookingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for advancing a booking through its lifecycle.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingStatusRequest {

    @NotNull(message = "Status is required")
    private BookingStatus status;
}
