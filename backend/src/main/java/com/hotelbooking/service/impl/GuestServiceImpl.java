package com.hotelbooking.service.impl;

import com.hotelbooking.dto.GuestRequest;
import com.hotelbooking.dto.GuestResponse;
import com.hotelbooking.entity.Guest;
import com.hotelbooking.exception.DuplicateGuestException;
import com.hotelbooking.exception.GuestHasBookingsException;
import com.hotelbooking.exception.GuestNotFoundException;
import com.hotelbooking.mapper.GuestMapper;
import com.hotelbooking.repository.BookingRepository;
import com.hotelbooking.repository.GuestRepository;
import com.hotelbooking.security.BookingOwnership;
import com.hotelbooking.service.GuestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuestServiceImpl implements GuestService {

    private final GuestRepository guestRepository;
    private final BookingRepository bookingRepository;
    private final GuestMapper guestMapper;
    private final BookingOwnership bookingOwnership;

    @Override
    @Transactional
    public GuestResponse createGuest(GuestRequest request) {
        log.info("Creating guest with email: {}", request.getEmail());
        bookingOwnership.assertGuestEmailAllowed(request.getEmail());
        validateDuplicateEmail(request.getEmail(), null);
        validateDuplicatePhone(request.getPhone(), null);

        Guest guest = guestMapper.toEntity(request);
        Guest savedGuest = guestRepository.save(guest);
        log.debug("Guest created with id: {}", savedGuest.getId());
        return guestMapper.toResponse(savedGuest);
    }

    @Override
    public GuestResponse getGuestById(Long id) {
        log.debug("Fetching guest by id: {}", id);
        Guest guest = findGuestOrThrow(id);
        return guestMapper.toResponse(guest);
    }

    @Override
    public Page<GuestResponse> getAllGuests(Pageable pageable) {
        log.debug("Fetching guests page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return guestRepository.findAll(pageable).map(guestMapper::toResponse);
    }

    @Override
    @Transactional
    public GuestResponse updateGuest(Long id, GuestRequest request) {
        log.info("Updating guest id: {}", id);
        bookingOwnership.assertCanAccessGuest(id);
        bookingOwnership.assertGuestEmailAllowed(request.getEmail());
        Guest guest = findGuestOrThrow(id);
        validateDuplicateEmail(request.getEmail(), id);
        validateDuplicatePhone(request.getPhone(), id);

        guestMapper.updateEntityFromRequest(request, guest);
        Guest updatedGuest = guestRepository.save(guest);
        log.debug("Guest updated with id: {}", updatedGuest.getId());
        return guestMapper.toResponse(updatedGuest);
    }

    @Override
    @Transactional
    public void deleteGuest(Long id) {
        log.info("Deleting guest id: {}", id);
        Guest guest = findGuestOrThrow(id);

        // Include cancelled rows (@SQLRestriction hides them from findByGuestId)
        if (bookingRepository.countAllRowsByGuestId(id) > 0) {
            throw new GuestHasBookingsException(id);
        }

        guestRepository.delete(guest);
        log.debug("Guest deleted with id: {}", id);
    }

    @Override
    public GuestResponse searchByEmail(String email) {
        log.debug("Searching guest by email: {}", email);
        return guestRepository.findByEmailIgnoreCase(email.trim())
                .map(guestMapper::toResponse)
                .orElseThrow(() -> new GuestNotFoundException("Guest not found with email: " + email));
    }

    @Override
    public GuestResponse searchByPhone(String phone) {
        log.debug("Searching guest by phone: {}", phone);
        return guestRepository.findByPhone(phone.trim())
                .map(guestMapper::toResponse)
                .orElseThrow(() -> new GuestNotFoundException("Guest not found with phone: " + phone));
    }

    @Override
    public List<GuestResponse> searchByName(String name) {
        log.debug("Searching guests by name: {}", name);
        return guestRepository.searchByName(name.trim()).stream()
                .map(guestMapper::toResponse)
                .toList();
    }

    private Guest findGuestOrThrow(Long id) {
        return guestRepository.findById(id)
                .orElseThrow(() -> new GuestNotFoundException(id));
    }

    private void validateDuplicateEmail(String email, Long currentGuestId) {
        boolean duplicate = currentGuestId == null
                ? guestRepository.existsByEmail(email)
                : guestRepository.existsByEmailAndIdNot(email, currentGuestId);

        if (duplicate) {
            throw new DuplicateGuestException("Guest already exists with email: " + email);
        }
    }

    private void validateDuplicatePhone(String phone, Long currentGuestId) {
        if (!StringUtils.hasText(phone)) {
            return;
        }

        boolean duplicate = currentGuestId == null
                ? guestRepository.existsByPhone(phone)
                : guestRepository.existsByPhoneAndIdNot(phone, currentGuestId);

        if (duplicate) {
            throw new DuplicateGuestException("Guest already exists with phone: " + phone);
        }
    }
}
