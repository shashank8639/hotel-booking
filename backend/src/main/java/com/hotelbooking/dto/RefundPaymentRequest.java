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
public class RefundPaymentRequest {

    @NotNull(message = "Payment ID is required")
    private Long paymentId;

    /**
     * Optional. When null, full refundable balance is refunded.
     */
    @DecimalMin(value = "0.01", message = "Refund amount must be at least 0.01")
    private BigDecimal amount;
}
