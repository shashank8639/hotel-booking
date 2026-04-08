package com.hotelbooking.service.impl;

import com.hotelbooking.database.RoomStatus;
import com.hotelbooking.database.RoomType;
import com.hotelbooking.dto.RoomAvailabilityCalendarResponse;
import com.hotelbooking.dto.RoomAvailabilityDayItem;
import com.hotelbooking.dto.RoomAvailabilityRequest;
import com.hotelbooking.dto.RoomDescriptionPatchRequest;
import com.hotelbooking.dto.RoomImageRequest;
import com.hotelbooking.dto.RoomImageResponse;
import com.hotelbooking.dto.RoomPricingRequest;
import com.hotelbooking.dto.RoomRequest;
import com.hotelbooking.dto.RoomResponse;
import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.Room;
import com.hotelbooking.entity.RoomImage;
import com.hotelbooking.exception.DuplicateRoomException;
import com.hotelbooking.exception.InvalidRoomPricingException;
import com.hotelbooking.exception.InvalidRoomStatusTransitionException;
import com.hotelbooking.exception.RoomImageNotFoundException;
import com.hotelbooking.exception.RoomNotFoundException;
import com.hotelbooking.mapper.RoomMapper;
import com.hotelbooking.repository.BookingRepository;
import com.hotelbooking.repository.RoomImageRepository;
import com.hotelbooking.repository.RoomRepository;
import com.hotelbooking.service.RoomService;
import com.hotelbooking.util.RoomPageables;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomServiceImpl implements RoomService {

    private static final Map<RoomStatus, Set<RoomStatus>> ALLOWED_TRANSITIONS = Map.of(
            RoomStatus.AVAILABLE, EnumSet.of(
                    RoomStatus.RESERVED, RoomStatus.CLEANING, RoomStatus.MAINTENANCE, RoomStatus.OUT_OF_SERVICE),
            RoomStatus.RESERVED, EnumSet.of(
                    RoomStatus.OCCUPIED, RoomStatus.AVAILABLE, RoomStatus.CLEANING, RoomStatus.MAINTENANCE),
            RoomStatus.OCCUPIED, EnumSet.of(RoomStatus.CLEANING, RoomStatus.MAINTENANCE),
            RoomStatus.CLEANING, EnumSet.of(RoomStatus.AVAILABLE, RoomStatus.MAINTENANCE),
            RoomStatus.MAINTENANCE, EnumSet.of(RoomStatus.AVAILABLE, RoomStatus.OUT_OF_SERVICE),
            RoomStatus.OUT_OF_SERVICE, EnumSet.of(RoomStatus.AVAILABLE, RoomStatus.MAINTENANCE)
    );

    private static final Set<RoomStatus> OPERATIONAL_BLOCKS = EnumSet.of(
            RoomStatus.CLEANING, RoomStatus.MAINTENANCE, RoomStatus.OUT_OF_SERVICE
    );

    private final RoomRepository roomRepository;
    private final RoomImageRepository roomImageRepository;
    private final BookingRepository bookingRepository;
    private final RoomMapper roomMapper;
    private final com.hotelbooking.repository.HotelRepository hotelRepository;

    @Override
    @Transactional
    public RoomResponse createRoom(RoomRequest request) {
        log.info("Creating room with number: {}", request.getRoomNumber());
        var hotel = resolveHotel(request.getHotelId());
        validateDuplicateRoomNumber(hotel.getId(), request.getRoomNumber(), null);
        validatePricing(request.getPricePerNight(), request.getDiscountedPrice());

        Room room = roomMapper.toEntity(request);
        room.setHotel(hotel);
        Room saved = roomRepository.save(room);
        log.debug("Room created with id: {}", saved.getId());
        return roomMapper.toResponse(saved);
    }

    @Override
    public RoomResponse getRoomById(Long id) {
        log.debug("Fetching room by id: {}", id);
        return roomMapper.toResponse(findRoomOrThrow(id));
    }

    @Override
    public Page<RoomResponse> getAllRooms(Pageable pageable) {
        Pageable constrained = RoomPageables.constrain(pageable);
        log.debug("Fetching rooms page: {}, size: {}", constrained.getPageNumber(), constrained.getPageSize());
        return roomRepository.findByDeletedFalse(constrained).map(roomMapper::toResponse);
    }

    @Override
    @Transactional
    public RoomResponse updateRoom(Long id, RoomRequest request) {
        log.info("Updating room id: {}", id);
        Room room = findRoomOrThrow(id);
        validateDuplicateRoomNumber(room.getHotel().getId(), request.getRoomNumber(), id);
        validatePricing(request.getPricePerNight(), request.getDiscountedPrice());

        if (request.getStatus() != null && request.getStatus() != room.getStatus()) {
            validateStatusTransition(room.getStatus(), request.getStatus());
        }

        roomMapper.updateEntityFromRequest(request, room);
        Room updated = roomRepository.save(room);
        return roomMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public RoomResponse patchDescription(Long id, RoomDescriptionPatchRequest request) {
        log.info("Patching description for room id: {}", id);
        Room room = findRoomOrThrow(id);
        room.setDescription(request.getDescription());
        return roomMapper.toResponse(roomRepository.save(room));
    }

    @Override
    @Transactional
    public void deleteRoom(Long id) {
        log.info("Soft-deleting room id: {}", id);
        Room room = findRoomOrThrow(id);
        room.setDeleted(true);
        roomRepository.save(room);
        log.debug("Room soft-deleted with id: {}", id);
    }

    @Override
    @Transactional
    public RoomResponse updateAvailability(Long id, RoomAvailabilityRequest request) {
        log.info("Updating availability for room id: {} to {}", id, request.getStatus());
        Room room = findRoomOrThrow(id);
        validateStatusTransition(room.getStatus(), request.getStatus());
        room.setStatus(request.getStatus());
        return roomMapper.toResponse(roomRepository.save(room));
    }

    @Override
    @Transactional
    public RoomResponse updatePricing(Long id, RoomPricingRequest request) {
        log.info("Updating pricing for room id: {}", id);
        Room room = findRoomOrThrow(id);
        validatePricing(request.getPricePerNight(), request.getDiscountedPrice());
        room.setPricePerNight(request.getPricePerNight());
        room.setDiscountedPrice(request.getDiscountedPrice());
        if (StringUtils.hasText(request.getCurrency())) {
            room.setCurrency(request.getCurrency().trim().toUpperCase());
        }
        return roomMapper.toResponse(roomRepository.save(room));
    }

    @Override
    public Page<RoomResponse> searchRooms(
            String roomNumber,
            RoomType roomType,
            RoomStatus status,
            Integer floorNumber,
            Integer minCapacity,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String description,
            Pageable pageable
    ) {
        Pageable constrained = RoomPageables.constrain(pageable);
        log.debug(
                "Searching rooms filters roomNumber={}, type={}, status={}, floor={}, minCapacity={}, minPrice={}, maxPrice={}, description={}",
                roomNumber, roomType, status, floorNumber, minCapacity, minPrice, maxPrice, description
        );

        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new InvalidRoomPricingException("minPrice cannot be greater than maxPrice");
        }

        String normalizedRoomNumber = StringUtils.hasText(roomNumber) ? roomNumber.trim() : null;
        String normalizedDescription = StringUtils.hasText(description) ? description.trim() : null;

        return roomRepository.searchRooms(
                normalizedRoomNumber,
                roomType,
                status,
                floorNumber,
                minCapacity,
                minPrice,
                maxPrice,
                normalizedDescription,
                constrained
        ).map(roomMapper::toResponse);
    }

    @Override
    public RoomAvailabilityCalendarResponse getAvailabilityCalendar(Long roomId, LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new InvalidRoomPricingException("from and to dates are required");
        }
        if (!to.isAfter(from)) {
            throw new InvalidRoomPricingException("to must be after from");
        }
        if (from.plusDays(90).isBefore(to)) {
            throw new InvalidRoomPricingException("Availability calendar range cannot exceed 90 nights");
        }

        Room room = findRoomOrThrow(roomId);
        List<Booking> overlaps = bookingRepository.findOverlappingBookings(roomId, from, to);

        Set<LocalDate> bookedNights = new HashSet<>();
        for (Booking booking : overlaps) {
            LocalDate start = booking.getCheckInDate().isBefore(from) ? from : booking.getCheckInDate();
            LocalDate end = booking.getCheckOutDate().isAfter(to) ? to : booking.getCheckOutDate();
            start.datesUntil(end).forEach(bookedNights::add);
        }

        String operationalBlock = OPERATIONAL_BLOCKS.contains(room.getStatus()) ? room.getStatus().name() : null;

        List<RoomAvailabilityDayItem> days = new ArrayList<>();
        from.datesUntil(to).forEach(date -> {
            if (operationalBlock != null) {
                days.add(RoomAvailabilityDayItem.builder()
                        .date(date)
                        .available(false)
                        .blockReason(operationalBlock)
                        .build());
            } else if (bookedNights.contains(date)) {
                days.add(RoomAvailabilityDayItem.builder()
                        .date(date)
                        .available(false)
                        .blockReason("BOOKED")
                        .build());
            } else {
                days.add(RoomAvailabilityDayItem.builder()
                        .date(date)
                        .available(true)
                        .blockReason(null)
                        .build());
            }
        });

        return RoomAvailabilityCalendarResponse.builder()
                .roomId(room.getId())
                .roomNumber(room.getRoomNumber())
                .from(from)
                .to(to)
                .days(days)
                .build();
    }

    @Override
    public List<RoomType> getRoomTypes() {
        return Arrays.asList(RoomType.values());
    }

    @Override
    public List<RoomStatus> getRoomStatuses() {
        return Arrays.asList(RoomStatus.values());
    }

    @Override
    @Transactional
    public RoomImageResponse addRoomImage(Long roomId, RoomImageRequest request) {
        log.info("Adding image to room id: {}", roomId);
        Room room = findRoomOrThrow(roomId);
        RoomImage image = roomMapper.toImageEntity(request);
        if (image.getDisplayOrder() == null) {
            image.setDisplayOrder(0);
        }
        if (request.getPrimary() == null) {
            image.setPrimary(false);
        }
        room.addImage(image);

        if (image.isPrimary()) {
            clearOtherPrimaryFlags(room, null);
        }

        RoomImage saved = roomImageRepository.save(image);
        return roomMapper.toImageResponse(saved);
    }

    @Override
    @Transactional
    public RoomImageResponse updateRoomImage(Long roomId, Long imageId, RoomImageRequest request) {
        log.info("Updating image id: {} for room id: {}", imageId, roomId);
        findRoomOrThrow(roomId);
        RoomImage image = roomImageRepository.findByIdAndRoomId(imageId, roomId)
                .orElseThrow(() -> new RoomImageNotFoundException(imageId, roomId));

        roomMapper.updateImageFromRequest(request, image);
        if (Boolean.TRUE.equals(request.getPrimary())) {
            clearOtherPrimaryFlags(image.getRoom(), imageId);
            image.setPrimary(true);
        }

        return roomMapper.toImageResponse(roomImageRepository.save(image));
    }

    @Override
    @Transactional
    public void deleteRoomImage(Long roomId, Long imageId) {
        log.info("Deleting image id: {} for room id: {}", imageId, roomId);
        findRoomOrThrow(roomId);
        RoomImage image = roomImageRepository.findByIdAndRoomId(imageId, roomId)
                .orElseThrow(() -> new RoomImageNotFoundException(imageId, roomId));
        roomImageRepository.delete(image);
    }

    @Override
    public List<RoomImageResponse> getRoomImages(Long roomId) {
        findRoomOrThrow(roomId);
        return roomMapper.toImageResponseList(roomImageRepository.findByRoomIdOrderByDisplayOrderAsc(roomId));
    }

    private Room findRoomOrThrow(Long id) {
        return roomRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RoomNotFoundException(id));
    }

    private void validateDuplicateRoomNumber(Long hotelId, String roomNumber, Long currentRoomId) {
        boolean duplicate = currentRoomId == null
                ? roomRepository.existsByHotel_IdAndRoomNumberAndDeletedFalse(hotelId, roomNumber)
                : roomRepository.existsByHotel_IdAndRoomNumberAndDeletedFalseAndIdNot(hotelId, roomNumber, currentRoomId);

        if (duplicate) {
            throw new DuplicateRoomException("Room already exists with number: " + roomNumber);
        }
    }

    private com.hotelbooking.entity.Hotel resolveHotel(Long hotelId) {
        if (hotelId != null) {
            return hotelRepository.findById(hotelId)
                    .orElseThrow(() -> new com.hotelbooking.exception.HotelNotFoundException(hotelId));
        }
        return hotelRepository.findBySlug("grand-horizon-hyderabad")
                .orElseThrow(() -> new IllegalStateException(
                        "Default hotel grand-horizon-hyderabad missing — apply V12 migration"));
    }

    private void validatePricing(BigDecimal pricePerNight, BigDecimal discountedPrice) {
        if (pricePerNight == null) {
            throw new InvalidRoomPricingException("Price per night is required");
        }
        if (pricePerNight.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidRoomPricingException("Price per night cannot be negative");
        }
        if (discountedPrice != null) {
            if (discountedPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidRoomPricingException("Discounted price cannot be negative");
            }
            if (discountedPrice.compareTo(pricePerNight) > 0) {
                throw new InvalidRoomPricingException("Discounted price cannot exceed base price");
            }
        }
    }

    private void validateStatusTransition(RoomStatus current, RoomStatus next) {
        if (current == next) {
            return;
        }
        Set<RoomStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(current, EnumSet.noneOf(RoomStatus.class));
        if (!allowed.contains(next)) {
            throw new InvalidRoomStatusTransitionException(
                    "Invalid room status transition from " + current + " to " + next
            );
        }
    }

    private void clearOtherPrimaryFlags(Room room, Long keepImageId) {
        for (RoomImage existing : room.getImages()) {
            if (keepImageId == null || !keepImageId.equals(existing.getId())) {
                existing.setPrimary(false);
            }
        }
    }
}
