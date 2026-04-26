package com.hotelbooking.service.impl;

import com.hotelbooking.database.HotelCategory;
import com.hotelbooking.database.HotelStatus;
import com.hotelbooking.database.ReviewStatus;
import com.hotelbooking.dto.RoomResponse;
import com.hotelbooking.dto.hotel.AmenityResponse;
import com.hotelbooking.dto.hotel.CityResponse;
import com.hotelbooking.dto.hotel.HotelDetailResponse;
import com.hotelbooking.dto.hotel.HotelImageResponse;
import com.hotelbooking.dto.hotel.HotelPolicyResponse;
import com.hotelbooking.dto.hotel.HotelReviewResponse;
import com.hotelbooking.dto.hotel.HotelSummaryResponse;
import com.hotelbooking.dto.hotel.HotelUpsertRequest;
import com.hotelbooking.entity.Amenity;
import com.hotelbooking.entity.City;
import com.hotelbooking.entity.Hotel;
import com.hotelbooking.entity.HotelImage;
import com.hotelbooking.entity.User;
import com.hotelbooking.exception.HotelNotFoundException;
import com.hotelbooking.exception.PaymentValidationException;
import com.hotelbooking.mapper.RoomMapper;
import com.hotelbooking.repository.AmenityRepository;
import com.hotelbooking.repository.CityRepository;
import com.hotelbooking.repository.HotelRepository;
import com.hotelbooking.repository.RoomRepository;
import com.hotelbooking.repository.UserRepository;
import com.hotelbooking.service.HotelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final CityRepository cityRepository;
    private final AmenityRepository amenityRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    @Override
    public Page<HotelSummaryResponse> search(
            String citySlug,
            String city,
            String name,
            HotelCategory category,
            Integer minStars,
            BigDecimal minRating,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean breakfastIncluded,
            Boolean freeCancellation,
            Boolean petFriendly,
            List<String> amenityCodes,
            Pageable pageable
    ) {
        Page<Hotel> page = hotelRepository.searchPublic(
                blankToNull(citySlug),
                blankToNull(city),
                blankToNull(name),
                category,
                minStars,
                minRating,
                minPrice,
                maxPrice,
                breakfastIncluded,
                freeCancellation,
                petFriendly,
                pageable
        );

        if (amenityCodes == null || amenityCodes.isEmpty()) {
            return page.map(this::toSummary);
        }

        List<String> codes = amenityCodes.stream()
                .filter(StringUtils::hasText)
                .map(c -> c.trim().toUpperCase(Locale.ROOT))
                .distinct()
                .toList();
        if (codes.isEmpty()) {
            return page.map(this::toSummary);
        }

        // Amenity filter is applied in memory — load the full filtered catalog then page
        // so totals stay correct for Telangana-sized datasets.
        Page<Hotel> unpaged = hotelRepository.searchPublic(
                blankToNull(citySlug),
                blankToNull(city),
                blankToNull(name),
                category,
                minStars,
                minRating,
                minPrice,
                maxPrice,
                breakfastIncluded,
                freeCancellation,
                petFriendly,
                Pageable.unpaged()
        );
        List<Hotel> all = unpaged.getContent();
        if (all.isEmpty()) {
            return Page.empty(pageable);
        }
        Set<Long> matching = hotelRepository
                .filterByAllAmenities(
                        all.stream().map(Hotel::getId).toList(),
                        codes,
                        codes.size()
                )
                .stream()
                .map(Hotel::getId)
                .collect(Collectors.toSet());
        List<Hotel> filtered = all.stream().filter(h -> matching.contains(h.getId())).toList();
        int start = (int) pageable.getOffset();
        if (start >= filtered.size()) {
            return new PageImpl<>(List.of(), pageable, filtered.size());
        }
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<HotelSummaryResponse> slice = filtered.subList(start, end).stream()
                .map(this::toSummary)
                .toList();
        return new PageImpl<>(slice, pageable, filtered.size());
    }

    @Override
    public HotelDetailResponse getBySlug(String slug) {
        Hotel hotel = hotelRepository.findBySlug(slug)
                .orElseThrow(() -> new HotelNotFoundException("Hotel not found: " + slug));
        assertPubliclyVisible(hotel);
        return toDetail(hotel);
    }

    @Override
    public HotelDetailResponse getById(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new HotelNotFoundException(id));
        return toDetail(hotel);
    }

    @Override
    public List<HotelSummaryResponse> featured() {
        return hotelRepository
                .findByFeaturedTrueAndStatusAndVerifiedTrueOrderByAvgRatingDesc(HotelStatus.APPROVED)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    @Override
    public List<CityResponse> popularCities() {
        return cityRepository.findByPopularTrueOrderByNameAsc().stream().map(this::toCity).toList();
    }

    @Override
    public List<CityResponse> listTelanganaCities() {
        return cityRepository.findByState_CodeOrderByNameAsc("TS").stream().map(this::toCity).toList();
    }

    @Override
    @Transactional
    public HotelDetailResponse submitForApproval(HotelUpsertRequest request, Long ownerUserId) {
        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new PaymentValidationException("Invalid city id"));
        User owner = userRepository.findById(ownerUserId)
                .orElseThrow(() -> new PaymentValidationException("Owner user not found"));

        String slug = uniqueSlug(request.getName(), city.getSlug());
        Hotel hotel = Hotel.builder()
                .city(city)
                .owner(owner)
                .name(request.getName().trim())
                .slug(slug)
                .description(request.getDescription())
                .category(request.getCategory() != null ? request.getCategory() : HotelCategory.HOTEL)
                .starRating(request.getStarRating() != null ? request.getStarRating() : 3)
                .status(HotelStatus.PENDING_APPROVAL)
                .verified(false)
                .featured(false)
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .postalCode(request.getPostalCode())
                .phone(request.getPhone())
                .email(request.getEmail())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .breakfastIncluded(Boolean.TRUE.equals(request.getBreakfastIncluded()))
                .freeCancellation(Boolean.TRUE.equals(request.getFreeCancellation()))
                .petFriendly(Boolean.TRUE.equals(request.getPetFriendly()))
                .build();

        if (request.getAmenityCodes() != null) {
            Set<Amenity> amenities = new HashSet<>();
            for (String code : request.getAmenityCodes()) {
                amenityRepository.findByCode(code.toUpperCase(Locale.ROOT)).ifPresent(amenities::add);
            }
            hotel.setAmenities(amenities);
        }

        if (StringUtils.hasText(request.getPrimaryImageUrl())) {
            hotel.getImages().add(HotelImage.builder()
                    .hotel(hotel)
                    .imageUrl(request.getPrimaryImageUrl())
                    .primary(true)
                    .displayOrder(0)
                    .build());
        }

        Hotel saved = hotelRepository.save(hotel);
        log.info("Hotel submitted for approval id={} slug={}", saved.getId(), saved.getSlug());
        return toDetail(saved);
    }

    @Override
    @Transactional
    public HotelDetailResponse approve(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException(hotelId));
        hotel.setStatus(HotelStatus.APPROVED);
        hotel.setVerified(true);
        hotel.setRejectionReason(null);
        return toDetail(hotelRepository.save(hotel));
    }

    @Override
    @Transactional
    public HotelDetailResponse reject(Long hotelId, String reason) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException(hotelId));
        hotel.setStatus(HotelStatus.REJECTED);
        hotel.setVerified(false);
        hotel.setRejectionReason(reason);
        return toDetail(hotelRepository.save(hotel));
    }

    @Override
    public List<HotelSummaryResponse> listPendingApproval() {
        return hotelRepository.findByStatusOrderByCreatedAtDesc(HotelStatus.PENDING_APPROVAL)
                .stream().map(this::toSummary).toList();
    }

    @Override
    public List<HotelSummaryResponse> listOwned(Long ownerUserId) {
        return hotelRepository.findByOwner_IdOrderByNameAsc(ownerUserId)
                .stream().map(this::toSummary).toList();
    }

    @Override
    public List<RoomResponse> listRoomsBySlug(String slug) {
        Hotel hotel = hotelRepository.findBySlug(slug)
                .orElseThrow(() -> new HotelNotFoundException("Hotel not found: " + slug));
        assertPubliclyVisible(hotel);
        return roomRepository.findByHotel_IdAndDeletedFalse(hotel.getId()).stream()
                .map(roomMapper::toResponse)
                .toList();
    }

    private void assertPubliclyVisible(Hotel hotel) {
        if (hotel.getStatus() != HotelStatus.APPROVED || !hotel.isVerified()) {
            throw new HotelNotFoundException("Hotel not available in public catalog");
        }
    }

    private HotelSummaryResponse toSummary(Hotel hotel) {
        String image = hotel.getImages().stream()
                .filter(HotelImage::isPrimary)
                .map(HotelImage::getImageUrl)
                .findFirst()
                .orElseGet(() -> hotel.getImages().stream().map(HotelImage::getImageUrl).findFirst().orElse(null));

        return HotelSummaryResponse.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .slug(hotel.getSlug())
                .description(hotel.getDescription())
                .category(hotel.getCategory())
                .starRating(hotel.getStarRating())
                .status(hotel.getStatus())
                .verified(hotel.isVerified())
                .featured(hotel.isFeatured())
                .avgRating(hotel.getAvgRating())
                .reviewCount(hotel.getReviewCount())
                .minPrice(hotel.getMinPrice())
                .currency(hotel.getCurrency())
                .cityName(hotel.getCity().getName())
                .citySlug(hotel.getCity().getSlug())
                .stateName(hotel.getCity().getState().getName())
                .primaryImageUrl(image)
                .breakfastIncluded(hotel.isBreakfastIncluded())
                .freeCancellation(hotel.isFreeCancellation())
                .petFriendly(hotel.isPetFriendly())
                .amenityCodes(hotel.getAmenities().stream().map(Amenity::getCode).sorted().toList())
                .build();
    }

    private HotelDetailResponse toDetail(Hotel hotel) {
        return HotelDetailResponse.builder()
                .summary(toSummary(hotel))
                .addressLine1(hotel.getAddressLine1())
                .addressLine2(hotel.getAddressLine2())
                .postalCode(hotel.getPostalCode())
                .phone(hotel.getPhone())
                .email(hotel.getEmail())
                .website(hotel.getWebsite())
                .checkInTime(hotel.getCheckInTime())
                .checkOutTime(hotel.getCheckOutTime())
                .latitude(hotel.getLatitude())
                .longitude(hotel.getLongitude())
                .images(hotel.getImages().stream()
                        .map(img -> HotelImageResponse.builder()
                                .id(img.getId())
                                .imageUrl(img.getImageUrl())
                                .caption(img.getCaption())
                                .displayOrder(img.getDisplayOrder())
                                .primary(img.isPrimary())
                                .build())
                        .toList())
                .amenities(hotel.getAmenities().stream()
                        .map(a -> AmenityResponse.builder()
                                .id(a.getId())
                                .code(a.getCode())
                                .name(a.getName())
                                .icon(a.getIcon())
                                .category(a.getCategory())
                                .build())
                        .toList())
                .policies(hotel.getPolicies().stream()
                        .map(p -> HotelPolicyResponse.builder()
                                .id(p.getId())
                                .policyType(p.getPolicyType())
                                .title(p.getTitle())
                                .body(p.getBody())
                                .build())
                        .toList())
                .reviews(hotel.getReviews().stream()
                        .filter(r -> r.getStatus() == ReviewStatus.PUBLISHED)
                        .map(r -> HotelReviewResponse.builder()
                                .id(r.getId())
                                .guestName(r.getGuestName())
                                .rating(r.getRating())
                                .title(r.getTitle())
                                .body(r.getBody())
                                .verifiedStay(r.isVerifiedStay())
                                .createdAt(r.getCreatedAt())
                                .build())
                        .toList())
                .build();
    }

    private CityResponse toCity(City city) {
        return CityResponse.builder()
                .id(city.getId())
                .name(city.getName())
                .slug(city.getSlug())
                .stateName(city.getState().getName())
                .stateCode(city.getState().getCode())
                .popular(city.isPopular())
                .latitude(city.getLatitude())
                .longitude(city.getLongitude())
                .build();
    }

    private String uniqueSlug(String name, String citySlug) {
        String base = slugify(name) + "-" + citySlug;
        String candidate = base;
        int i = 2;
        while (hotelRepository.findBySlug(candidate).isPresent()) {
            candidate = base + "-" + i++;
        }
        return candidate;
    }

    private static String slugify(String input) {
        String now = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return now.isBlank() ? "hotel" : now;
    }

    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
