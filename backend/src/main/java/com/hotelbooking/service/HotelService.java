package com.hotelbooking.service;

import com.hotelbooking.database.HotelCategory;
import com.hotelbooking.dto.RoomResponse;
import com.hotelbooking.dto.hotel.CityResponse;
import com.hotelbooking.dto.hotel.HotelDetailResponse;
import com.hotelbooking.dto.hotel.HotelSummaryResponse;
import com.hotelbooking.dto.hotel.HotelUpsertRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface HotelService {

    Page<HotelSummaryResponse> search(
            String citySlug,
            String city,
            String name,
            HotelCategory category,
            Integer minStars,
            BigDecimal minRating,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean breakfastIncluded,
            Boolean freeCancellation,
            Boolean petFriendly,
            List<String> amenityCodes,
            Pageable pageable
    );

    HotelDetailResponse getBySlug(String slug);

    HotelDetailResponse getById(Long id);

    List<HotelSummaryResponse> featured();

    List<CityResponse> popularCities();

    List<CityResponse> listTelanganaCities();

    HotelDetailResponse submitForApproval(HotelUpsertRequest request, Long ownerUserId);

    HotelDetailResponse approve(Long hotelId);

    HotelDetailResponse reject(Long hotelId, String reason);

    List<HotelSummaryResponse> listPendingApproval();

    List<HotelSummaryResponse> listOwned(Long ownerUserId);

    List<RoomResponse> listRoomsBySlug(String slug);
}
