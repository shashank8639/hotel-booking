package com.hotelbooking.repository;

import com.hotelbooking.database.HotelCategory;
import com.hotelbooking.database.HotelStatus;
import com.hotelbooking.entity.Hotel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface HotelRepository extends JpaRepository<Hotel, Long>, JpaSpecificationExecutor<Hotel> {

    Optional<Hotel> findBySlug(String slug);

    List<Hotel> findByFeaturedTrueAndStatusAndVerifiedTrueOrderByAvgRatingDesc(HotelStatus status);

    @Query("""
            SELECT h FROM Hotel h
            JOIN h.city c
            WHERE h.status = com.hotelbooking.database.HotelStatus.APPROVED
              AND h.verified = true
              AND (:citySlug IS NULL OR c.slug = :citySlug)
              AND (:cityName IS NULL OR c.name LIKE CONCAT('%', :cityName, '%'))
              AND (:name IS NULL OR h.name LIKE CONCAT('%', :name, '%'))
              AND (:category IS NULL OR h.category = :category)
              AND (:minStars IS NULL OR h.starRating >= :minStars)
              AND (:minRating IS NULL OR h.avgRating >= :minRating)
              AND (:minPrice IS NULL OR h.minPrice IS NULL OR h.minPrice >= :minPrice)
              AND (:maxPrice IS NULL OR h.minPrice IS NULL OR h.minPrice <= :maxPrice)
              AND (:breakfast IS NULL OR h.breakfastIncluded = :breakfast)
              AND (:freeCancel IS NULL OR h.freeCancellation = :freeCancel)
              AND (:petFriendly IS NULL OR h.petFriendly = :petFriendly)
            """)
    Page<Hotel> searchPublic(
            @Param("citySlug") String citySlug,
            @Param("cityName") String cityName,
            @Param("name") String name,
            @Param("category") HotelCategory category,
            @Param("minStars") Integer minStars,
            @Param("minRating") BigDecimal minRating,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("breakfast") Boolean breakfast,
            @Param("freeCancel") Boolean freeCancel,
            @Param("petFriendly") Boolean petFriendly,
            Pageable pageable
    );

    @Query("""
            SELECT h FROM Hotel h
            JOIN h.amenities a
            WHERE h.id IN :hotelIds
              AND a.code IN :amenityCodes
            GROUP BY h
            HAVING COUNT(DISTINCT a.code) = :amenityCount
            """)
    List<Hotel> filterByAllAmenities(
            @Param("hotelIds") List<Long> hotelIds,
            @Param("amenityCodes") List<String> amenityCodes,
            @Param("amenityCount") long amenityCount
    );

    List<Hotel> findByOwner_IdOrderByNameAsc(Long ownerUserId);

    List<Hotel> findByStatusOrderByCreatedAtDesc(HotelStatus status);
}
