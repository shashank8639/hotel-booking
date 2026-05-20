package com.hotelbooking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomPricingRequest {

    @NotNull(message = "Price per night is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price per night must be zero or positive")
    private BigDecimal pricePerNight;

    @DecimalMin(value = "0.0", inclusive = true, message = "Discounted price must be zero or positive")
    private BigDecimal discountedPrice;
}
