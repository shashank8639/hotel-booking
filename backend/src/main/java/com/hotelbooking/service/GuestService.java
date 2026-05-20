package com.hotelbooking.service;

import com.hotelbooking.dto.GuestRequest;
import com.hotelbooking.dto.GuestResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GuestService {

    GuestResponse createGuest(GuestRequest request);

    GuestResponse getGuestById(Long id);

    Page<GuestResponse> getAllGuests(Pageable pageable);

    GuestResponse updateGuest(Long id, GuestRequest request);

    void deleteGuest(Long id);

    GuestResponse searchByEmail(String email);

    GuestResponse searchByPhone(String phone);

    List<GuestResponse> searchByName(String name);
}
