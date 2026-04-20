package com.hotelbooking.dto.hotel;

import com.hotelbooking.database.HotelCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelUpsertRequest {
    @NotBlank
    private String name;
    @NotNull
    private Long cityId;
    private String description;
    private HotelCategory category;
    private Integer starRating;
    @NotBlank
    private String addressLine1;
    private String addressLine2;
    private String postalCode;
    private String phone;
    private String email;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Boolean breakfastIncluded;
    private Boolean freeCancellation;
    private Boolean petFriendly;
    private List<String> amenityCodes;
    private String primaryImageUrl;
}
