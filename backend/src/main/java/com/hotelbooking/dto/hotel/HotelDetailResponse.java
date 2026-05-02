package com.hotelbooking.dto.hotel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelDetailResponse {
    private HotelSummaryResponse summary;
    private String addressLine1;
    private String addressLine2;
    private String postalCode;
    private String phone;
    private String email;
    private String website;
    private String checkInTime;
    private String checkOutTime;
    private java.math.BigDecimal latitude;
    private java.math.BigDecimal longitude;
    private List<HotelImageResponse> images;
    private List<AmenityResponse> amenities;
    private List<HotelPolicyResponse> policies;
    private List<HotelReviewResponse> reviews;
}
