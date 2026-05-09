package com.hotelbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponse {

    private String invoiceNumber;
    private String hotelName;
    private Long bookingId;
    private Long paymentId;
    private String guestName;
    private String guestEmail;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer numberOfNights;
    private List<InvoiceLineItem> rooms;
    private BigDecimal subtotal;
    private BigDecimal gstRate;
    private BigDecimal gstAmount;
    private BigDecimal totalAmount;
    /** exclusive = GST added on top of room subtotal; inclusive = GST embedded in total */
    private String taxModel;
    private String currency;
    private String baseCurrency;
    private BigDecimal fxRate;
    private BigDecimal amountInBase;
    private String paymentStatus;
    private String razorpayPaymentId;
    private String paidAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InvoiceLineItem {
        private String roomNumber;
        private String roomType;
        private BigDecimal pricePerNight;
        private Integer nights;
        private BigDecimal subtotal;
    }
}
