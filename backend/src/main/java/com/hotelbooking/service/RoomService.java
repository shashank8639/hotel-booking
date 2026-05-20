package com.hotelbooking.service;

import com.hotelbooking.database.RoomStatus;
import com.hotelbooking.database.RoomType;
import com.hotelbooking.dto.RoomAvailabilityRequest;
import com.hotelbooking.dto.RoomImageRequest;
import com.hotelbooking.dto.RoomImageResponse;
import com.hotelbooking.dto.RoomPricingRequest;
import com.hotelbooking.dto.RoomRequest;
import com.hotelbooking.dto.RoomResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface RoomService {

    RoomResponse createRoom(RoomRequest request);

    RoomResponse getRoomById(Long id);

    Page<RoomResponse> getAllRooms(Pageable pageable);

    RoomResponse updateRoom(Long id, RoomRequest request);

    void deleteRoom(Long id);

    RoomResponse updateAvailability(Long id, RoomAvailabilityRequest request);

    RoomResponse updatePricing(Long id, RoomPricingRequest request);

    Page<RoomResponse> searchRooms(
            String roomNumber,
            RoomType roomType,
            RoomStatus status,
            Integer minCapacity,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    );

    List<RoomType> getRoomTypes();

    List<RoomStatus> getRoomStatuses();

    RoomImageResponse addRoomImage(Long roomId, RoomImageRequest request);

    RoomImageResponse updateRoomImage(Long roomId, Long imageId, RoomImageRequest request);

    void deleteRoomImage(Long roomId, Long imageId);

    List<RoomImageResponse> getRoomImages(Long roomId);
}
