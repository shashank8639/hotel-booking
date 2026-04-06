package com.hotelbooking.controller;

import com.hotelbooking.dto.GuestRequest;
import com.hotelbooking.dto.GuestResponse;
import com.hotelbooking.service.GuestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/guests")
@RequiredArgsConstructor
@Validated
@Tag(name = "Guests", description = "Guest management APIs")
public class GuestController {

    private final GuestService guestService;

    @Operation(summary = "Create a new guest (customers: own email only)")
    @PostMapping
    public ResponseEntity<GuestResponse> createGuest(@Valid @RequestBody GuestRequest request) {
        GuestResponse response = guestService.createGuest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get guest by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @bookingOwnership.canAccessGuest(#id)")
    public ResponseEntity<GuestResponse> getGuestById(@PathVariable Long id) {
        return ResponseEntity.ok(guestService.getGuestById(id));
    }

    @Operation(summary = "Get all guests with pagination and sorting (ADMIN)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<GuestResponse>> getAllGuests(
            @PageableDefault(size = 10, sort = "lastName", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(guestService.getAllGuests(pageable));
    }

    @Operation(summary = "Update guest by ID")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @bookingOwnership.canAccessGuest(#id)")
    public ResponseEntity<GuestResponse> updateGuest(
            @PathVariable Long id,
            @Valid @RequestBody GuestRequest request
    ) {
        return ResponseEntity.ok(guestService.updateGuest(id, request));
    }

    @Operation(summary = "Delete guest by ID (ADMIN)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteGuest(@PathVariable Long id) {
        guestService.deleteGuest(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Search guest by email (customers: own email only)")
    @GetMapping("/search/email")
    @PreAuthorize("hasRole('ADMIN') or @bookingOwnership.canSearchGuestEmail(#email)")
    public ResponseEntity<GuestResponse> searchByEmail(
            @RequestParam @NotBlank(message = "Email parameter is required") String email
    ) {
        return ResponseEntity.ok(guestService.searchByEmail(email));
    }

    @Operation(summary = "Search guest by phone number (ADMIN)")
    @GetMapping("/search/phone")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GuestResponse> searchByPhone(
            @RequestParam @NotBlank(message = "Phone parameter is required") String phone
    ) {
        return ResponseEntity.ok(guestService.searchByPhone(phone));
    }

    @Operation(summary = "Search guests by full or partial name (ADMIN)")
    @GetMapping("/search/name")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<GuestResponse>> searchByName(
            @RequestParam @NotBlank(message = "Name parameter is required") String name
    ) {
        return ResponseEntity.ok(guestService.searchByName(name));
    }
}
