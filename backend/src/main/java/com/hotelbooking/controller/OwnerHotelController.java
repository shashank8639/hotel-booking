package com.hotelbooking.controller;

import com.hotelbooking.dto.hotel.HotelDetailResponse;
import com.hotelbooking.dto.hotel.HotelSummaryResponse;
import com.hotelbooking.dto.hotel.HotelUpsertRequest;
import com.hotelbooking.security.CustomUserDetails;
import com.hotelbooking.service.HotelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/owner/hotels")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('HOTEL_OWNER', 'ADMIN')")
public class OwnerHotelController {

    private final HotelService hotelService;

    @GetMapping
    public ResponseEntity<List<HotelSummaryResponse>> mine(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ResponseEntity.ok(hotelService.listOwned(user.getUser().getId()));
    }

    @PostMapping
    public ResponseEntity<HotelDetailResponse> register(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody HotelUpsertRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(hotelService.submitForApproval(request, user.getUser().getId()));
    }
}
