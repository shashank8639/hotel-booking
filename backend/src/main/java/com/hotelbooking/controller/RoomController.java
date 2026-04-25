package com.hotelbooking.controller;

import com.hotelbooking.database.RoomStatus;
import com.hotelbooking.database.RoomType;
import com.hotelbooking.dto.RoomAvailabilityCalendarResponse;
import com.hotelbooking.dto.RoomImageResponse;
import com.hotelbooking.dto.RoomResponse;
import com.hotelbooking.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Public/customer-facing room read and search APIs.
 * Mutations live under {@code /admin/rooms}.
 */
@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
@Tag(name = "Rooms", description = "Public room catalog APIs")
public class RoomController {

    private final RoomService roomService;

    @Operation(summary = "List rooms with pagination and sorting (max size 50; whitelisted sort fields)")
    @GetMapping
    public ResponseEntity<Page<RoomResponse>> getAllRooms(
            @PageableDefault(size = 10, sort = "roomNumber", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(roomService.getAllRooms(pageable));
    }

    @Operation(summary = "Search rooms with combined filters (floor, description keywords, …)")
    @GetMapping("/search")
    public ResponseEntity<Page<RoomResponse>> searchRooms(
            @RequestParam(required = false) String roomNumber,
            @RequestParam(required = false) RoomType roomType,
            @RequestParam(required = false) RoomStatus status,
            @RequestParam(required = false) Integer floorNumber,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String description,
            @PageableDefault(size = 10, sort = "pricePerNight", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(roomService.searchRooms(
                roomNumber, roomType, status, floorNumber, minCapacity, minPrice, maxPrice, description, pageable
        ));
    }

    @Operation(summary = "List supported room types")
    @GetMapping("/types")
    public ResponseEntity<List<RoomType>> getRoomTypes() {
        return ResponseEntity.ok(roomService.getRoomTypes());
    }

    @Operation(summary = "List supported room availability statuses")
    @GetMapping("/statuses")
    public ResponseEntity<List<RoomStatus>> getRoomStatuses() {
        return ResponseEntity.ok(roomService.getRoomStatuses());
    }

    @Operation(summary = "Day-by-day availability calendar for a room (from inclusive, to exclusive)")
    @GetMapping("/{id}/availability-calendar")
    public ResponseEntity<RoomAvailabilityCalendarResponse> getAvailabilityCalendar(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(roomService.getAvailabilityCalendar(id, from, to));
    }

    @Operation(summary = "Get images for a room")
    @GetMapping("/{id}/images")
    public ResponseEntity<List<RoomImageResponse>> getRoomImages(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomImages(id));
    }

    @Operation(summary = "Get room by ID")
    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }
}
