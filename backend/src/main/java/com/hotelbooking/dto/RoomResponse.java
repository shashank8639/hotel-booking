package com.hotelbooking.dto;

import com.hotelbooking.database.RoomStatus;
import com.hotelbooking.database.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomResponse {

    private Long id;
    private Long hotelId;
    private String hotelName;
    private String hotelSlug;
    private String roomNumber;
    private RoomType roomType;
    private Integer floorNumber;
    private Integer capacity;
    private BigDecimal pricePerNight;
    private BigDecimal discountedPrice;
    private BigDecimal effectivePrice;
    private String currency;
    private RoomStatus status;
    private String description;
    private boolean deleted;
    private List<RoomImageResponse> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
