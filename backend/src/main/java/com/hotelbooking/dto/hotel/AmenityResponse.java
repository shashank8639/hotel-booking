package com.hotelbooking.dto.hotel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmenityResponse {
    private Long id;
    private String code;
    private String name;
    private String icon;
    private String category;
}
