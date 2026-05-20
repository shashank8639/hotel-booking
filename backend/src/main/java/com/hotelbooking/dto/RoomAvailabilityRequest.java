package com.hotelbooking.dto;

import com.hotelbooking.database.RoomStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomAvailabilityRequest {

    @NotNull(message = "Status is required")
    private RoomStatus status;
}
