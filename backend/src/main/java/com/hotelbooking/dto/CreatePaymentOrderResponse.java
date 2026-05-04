package com.hotelbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentOrderResponse {

    private Long paymentId;
    private Long bookingId;
    private String razorpayOrderId;
    private String razorpayKeyId;
    private BigDecimal amount;
    private String currency;
    private String baseCurrency;
    private BigDecimal fxRate;
    private BigDecimal amountInBase;
    private BigDecimal taxableAmount;
    private BigDecimal gstAmount;
    private String status;
}
