package com.hotelbooking.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignPaymentRequest {

    @NotBlank
    private String razorpayOrderId;

    @NotBlank
    private String razorpayPaymentId;
}
