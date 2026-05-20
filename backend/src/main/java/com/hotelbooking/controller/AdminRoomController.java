package com.hotelbooking.controller;

import com.hotelbooking.dto.RoomAvailabilityRequest;
import com.hotelbooking.dto.RoomImageRequest;
import com.hotelbooking.dto.RoomImageResponse;
import com.hotelbooking.dto.RoomPricingRequest;
import com.hotelbooking.dto.RoomRequest;
import com.hotelbooking.dto.RoomResponse;
import com.hotelbooking.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin-only room management APIs.
 * Protected by SecurityConfig rule: {@code /admin/**} requires ROLE_ADMIN.
 */
@RestController
@RequestMapping("/admin/rooms")
@RequiredArgsConstructor
@Tag(name = "Admin Rooms", description = "Admin room management APIs")
public class AdminRoomController {

    private final RoomService roomService;

    @Operation(summary = "Create a room")
    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody RoomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.createRoom(request));
    }

    @Operation(summary = "Update a room")
    @PutMapping("/{id}")
    public ResponseEntity<RoomResponse> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody RoomRequest request
    ) {
        return ResponseEntity.ok(roomService.updateRoom(id, request));
    }

    @Operation(summary = "Delete a room")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update room availability status")
    @PutMapping("/{id}/availability")
    public ResponseEntity<RoomResponse> updateAvailability(
            @PathVariable Long id,
            @Valid @RequestBody RoomAvailabilityRequest request
    ) {
        return ResponseEntity.ok(roomService.updateAvailability(id, request));
    }

    @Operation(summary = "Update room pricing")
    @PutMapping("/{id}/pricing")
    public ResponseEntity<RoomResponse> updatePricing(
            @PathVariable Long id,
            @Valid @RequestBody RoomPricingRequest request
    ) {
        return ResponseEntity.ok(roomService.updatePricing(id, request));
    }

    @Operation(summary = "Add room image metadata/URL")
    @PostMapping("/{id}/images")
    public ResponseEntity<RoomImageResponse> addRoomImage(
            @PathVariable Long id,
            @Valid @RequestBody RoomImageRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.addRoomImage(id, request));
    }

    @Operation(summary = "Update room image metadata/URL")
    @PutMapping("/{roomId}/images/{imageId}")
    public ResponseEntity<RoomImageResponse> updateRoomImage(
            @PathVariable Long roomId,
            @PathVariable Long imageId,
            @Valid @RequestBody RoomImageRequest request
    ) {
        return ResponseEntity.ok(roomService.updateRoomImage(roomId, imageId, request));
    }

    @Operation(summary = "Delete room image")
    @DeleteMapping("/{roomId}/images/{imageId}")
    public ResponseEntity<Void> deleteRoomImage(
            @PathVariable Long roomId,
            @PathVariable Long imageId
    ) {
        roomService.deleteRoomImage(roomId, imageId);
        return ResponseEntity.noContent().build();
    }
}
