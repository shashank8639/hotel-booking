package com.hotelbooking.dto.hotel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CityResponse {
    private Long id;
    private String name;
    private String slug;
    private String stateName;
    private String stateCode;
    private boolean popular;
    private java.math.BigDecimal latitude;
    private java.math.BigDecimal longitude;
}
