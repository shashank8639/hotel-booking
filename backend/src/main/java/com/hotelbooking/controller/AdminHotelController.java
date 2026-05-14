package com.hotelbooking.controller;

import com.hotelbooking.dto.hotel.HotelDetailResponse;
import com.hotelbooking.dto.hotel.HotelSummaryResponse;
import com.hotelbooking.dto.hotel.HotelUpsertRequest;
import com.hotelbooking.security.CustomUserDetails;
import com.hotelbooking.service.HotelService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/hotels")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminHotelController {

    private final HotelService hotelService;

    @GetMapping("/pending")
    public ResponseEntity<List<HotelSummaryResponse>> pending() {
        return ResponseEntity.ok(hotelService.listPendingApproval());
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<HotelDetailResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(hotelService.approve(id));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<HotelDetailResponse> reject(
            @PathVariable Long id,
            @Valid @RequestBody RejectRequest body
    ) {
        return ResponseEntity.ok(hotelService.reject(id, body.getReason()));
    }

    @Data
    public static class RejectRequest {
        @NotBlank
        private String reason;
    }
}
