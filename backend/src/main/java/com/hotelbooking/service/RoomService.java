package com.hotelbooking.service;

import com.hotelbooking.database.RoomStatus;
import com.hotelbooking.database.RoomType;
import com.hotelbooking.dto.RoomAvailabilityCalendarResponse;
import com.hotelbooking.dto.RoomAvailabilityRequest;
import com.hotelbooking.dto.RoomDescriptionPatchRequest;
import com.hotelbooking.dto.RoomImageRequest;
import com.hotelbooking.dto.RoomImageResponse;
import com.hotelbooking.dto.RoomPricingRequest;
import com.hotelbooking.dto.RoomRequest;
import com.hotelbooking.dto.RoomResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface RoomService {

    RoomResponse createRoom(RoomRequest request);

    RoomResponse getRoomById(Long id);

    Page<RoomResponse> getAllRooms(Pageable pageable);

    RoomResponse updateRoom(Long id, RoomRequest request);

    RoomResponse patchDescription(Long id, RoomDescriptionPatchRequest request);

    void deleteRoom(Long id);

    RoomResponse updateAvailability(Long id, RoomAvailabilityRequest request);

    RoomResponse updatePricing(Long id, RoomPricingRequest request);

    Page<RoomResponse> searchRooms(
            String roomNumber,
            RoomType roomType,
            RoomStatus status,
            Integer floorNumber,
            Integer minCapacity,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String description,
            Pageable pageable
    );

    RoomAvailabilityCalendarResponse getAvailabilityCalendar(Long roomId, LocalDate from, LocalDate to);

    List<RoomType> getRoomTypes();

    List<RoomStatus> getRoomStatuses();

    RoomImageResponse addRoomImage(Long roomId, RoomImageRequest request);

    RoomImageResponse updateRoomImage(Long roomId, Long imageId, RoomImageRequest request);

    void deleteRoomImage(Long roomId, Long imageId);

    List<RoomImageResponse> getRoomImages(Long roomId);
}
