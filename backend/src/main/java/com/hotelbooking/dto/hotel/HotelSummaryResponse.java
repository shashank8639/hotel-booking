package com.hotelbooking.dto.hotel;

import com.hotelbooking.database.HotelCategory;
import com.hotelbooking.database.HotelStatus;
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
public class HotelSummaryResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private HotelCategory category;
    private int starRating;
    private HotelStatus status;
    private boolean verified;
    private boolean featured;
    private BigDecimal avgRating;
    private int reviewCount;
    private BigDecimal minPrice;
    private String currency;
    private String cityName;
    private String citySlug;
    private String stateName;
    private String primaryImageUrl;
    private boolean breakfastIncluded;
    private boolean freeCancellation;
    private boolean petFriendly;
    private List<String> amenityCodes;
}
