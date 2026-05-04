package com.hotelbooking.controller;

import com.hotelbooking.database.HotelCategory;
import com.hotelbooking.dto.RoomResponse;
import com.hotelbooking.dto.hotel.HotelDetailResponse;
import com.hotelbooking.dto.hotel.HotelSummaryResponse;
import com.hotelbooking.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * Public multi-hotel catalog & search (Module 16).
 */
@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @GetMapping("/search")
    public ResponseEntity<Page<HotelSummaryResponse>> search(
            @RequestParam(required = false) String citySlug,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) HotelCategory category,
            @RequestParam(required = false) Integer minStars,
            @RequestParam(required = false) BigDecimal minRating,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean breakfastIncluded,
            @RequestParam(required = false) Boolean freeCancellation,
            @RequestParam(required = false) Boolean petFriendly,
            @RequestParam(required = false) List<String> amenities,
            @PageableDefault(size = 12) Pageable pageable
    ) {
        return ResponseEntity.ok(hotelService.search(
                citySlug, city, name, category, minStars, minRating, minPrice, maxPrice,
                breakfastIncluded, freeCancellation, petFriendly, amenities, pageable
        ));
    }

    @GetMapping("/featured")
    public ResponseEntity<List<HotelSummaryResponse>> featured() {
        return ResponseEntity.ok(hotelService.featured());
    }

    @GetMapping("/{slug}")
    public ResponseEntity<HotelDetailResponse> bySlug(@PathVariable String slug) {
        return ResponseEntity.ok(hotelService.getBySlug(slug));
    }

    @GetMapping("/{slug}/rooms")
    public ResponseEntity<List<RoomResponse>> roomsForHotel(@PathVariable String slug) {
        return ResponseEntity.ok(hotelService.listRoomsBySlug(slug));
    }
}
