package com.hotelbooking.controller;

import com.hotelbooking.database.BookingStatus;
import com.hotelbooking.dto.AvailabilityResponse;
import com.hotelbooking.dto.BookingRequest;
import com.hotelbooking.dto.BookingResponse;
import com.hotelbooking.dto.BookingStatusRequest;
import com.hotelbooking.dto.RoomAvailabilityMatrixResponse;
import com.hotelbooking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Booking Engine REST API.
 * <p>
 * Protected by SecurityConfig: {@code /bookings/**} requires ROLE_ADMIN or ROLE_CUSTOMER.
 */
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Validated
@Tag(name = "Bookings", description = "Booking engine APIs")
public class BookingController {

    private final BookingService bookingService;

    @Operation(summary = "Create a booking for one or more rooms (PENDING soft-hold starts)")
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(request));
    }

    @Operation(summary = "Check room availability for a date range")
    @GetMapping("/availability")
    public ResponseEntity<AvailabilityResponse> checkAvailability(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            @RequestParam(required = false) List<Long> roomIds,
            @RequestParam(required = false) Long excludeBookingId
    ) {
        return ResponseEntity.ok(bookingService.checkAvailability(
                checkInDate, checkOutDate, roomIds, excludeBookingId
        ));
    }

    @Operation(summary = "Availability matrix: days × all rooms (max 30 days)")
    @GetMapping("/availability-matrix")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomAvailabilityMatrixResponse> getAvailabilityMatrix(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false, defaultValue = "30") Integer days
    ) {
        return ResponseEntity.ok(bookingService.getAvailabilityMatrix(from, days));
    }

    @Operation(summary = "Guest 'my bookings' with optional status / check-in filters")
    @GetMapping("/guest/{guestId}")
    @PreAuthorize("hasRole('ADMIN') or @bookingOwnership.canAccessGuest(#guestId)")
    public ResponseEntity<Page<BookingResponse>> getBookingsByGuest(
            @PathVariable Long guestId,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(size = 10, sort = "checkInDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(bookingService.getBookingsByGuest(guestId, status, from, to, pageable));
    }

    @Operation(summary = "List bookings by status (ADMIN)")
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<BookingResponse>> getBookingsByStatus(
            @PathVariable BookingStatus status,
            @PageableDefault(size = 10, sort = "checkInDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(bookingService.getBookingsByStatus(status, pageable));
    }

    @Operation(summary = "Get booking by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @bookingOwnership.canAccess(#id)")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @Operation(summary = "List all bookings (paginated; sort whitelist + size cap 50)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<BookingResponse>> getAllBookings(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(bookingService.getAllBookings(pageable));
    }

    @Operation(summary = "Cancel a booking (PENDING or CONFIRMED only)")
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or @bookingOwnership.canAccess(#id)")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.cancelBooking(id));
    }

    @Operation(summary = "Update booking status (confirm, check-in, check-out)")
    @PutMapping("/{id}/status")
    @PreAuthorize("@bookingOwnership.canModifyStatus(#id)")
    public ResponseEntity<BookingResponse> updateBookingStatus(
            @PathVariable Long id,
            @Valid @RequestBody BookingStatusRequest request
    ) {
        return ResponseEntity.ok(bookingService.updateBookingStatus(id, request));
    }
}
