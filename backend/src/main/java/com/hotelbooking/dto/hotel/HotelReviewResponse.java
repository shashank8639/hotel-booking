package com.hotelbooking.dto.hotel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelReviewResponse {
    private Long id;
    private String guestName;
    private int rating;
    private String title;
    private String body;
    private boolean verifiedStay;
    private LocalDateTime createdAt;
}
