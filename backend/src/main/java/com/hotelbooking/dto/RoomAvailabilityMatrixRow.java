package com.hotelbooking.dto;

import com.hotelbooking.database.RoomStatus;
import com.hotelbooking.database.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomAvailabilityMatrixRow {

    private Long roomId;
    private String roomNumber;
    private RoomType roomType;
    private RoomStatus roomStatus;
    private List<RoomAvailabilityDayItem> days;
}
