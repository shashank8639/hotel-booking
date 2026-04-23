package com.hotelbooking.dto;

import jakarta.validation.constraints.DecimalMin;
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
public class RoomPricingRequest {

    @NotNull(message = "Price per night is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price per night must be zero or positive")
    private BigDecimal pricePerNight;

    @DecimalMin(value = "0.0", inclusive = true, message = "Discounted price must be zero or positive")
    private BigDecimal discountedPrice;

    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO-4217 code")
    private String currency;
}
