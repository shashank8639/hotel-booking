package com.hotelbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomImageResponse {

    private Long id;
    private Long roomId;
    private String imageUrl;
    private String caption;
    private Integer displayOrder;
    private boolean primary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
