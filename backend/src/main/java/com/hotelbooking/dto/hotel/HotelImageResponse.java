package com.hotelbooking.dto.hotel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelImageResponse {
    private Long id;
    private String imageUrl;
    private String caption;
    private int displayOrder;
    private boolean primary;
}
