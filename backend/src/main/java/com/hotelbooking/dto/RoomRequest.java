package com.hotelbooking.dto;

import com.hotelbooking.database.RoomStatus;
import com.hotelbooking.database.RoomType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomRequest {

    @NotBlank(message = "Room number is required")
    @Size(max = 20, message = "Room number must not exceed 20 characters")
    private String roomNumber;

    @NotNull(message = "Room type is required")
    private RoomType roomType;

    private Integer floorNumber;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    @NotNull(message = "Price per night is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price per night must be zero or positive")
    private BigDecimal pricePerNight;

    @DecimalMin(value = "0.0", inclusive = true, message = "Discounted price must be zero or positive")
    private BigDecimal discountedPrice;

    private RoomStatus status;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;
}
