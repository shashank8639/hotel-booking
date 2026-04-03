package com.hotelbooking.repository;

import com.hotelbooking.entity.SeasonalRoomPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeasonalRoomPriceRepository extends JpaRepository<SeasonalRoomPrice, Long> {

    List<SeasonalRoomPrice> findByRoomIdOrderByStartDateAsc(Long roomId);

    /**
     * Prefer the most specific (latest-starting) season covering the night.
     */
    @Query("""
            SELECT s FROM SeasonalRoomPrice s
            WHERE s.room.id = :roomId
              AND s.startDate <= :night
              AND s.endDate > :night
            ORDER BY s.startDate DESC
            """)
    List<SeasonalRoomPrice> findCoveringNight(
            @Param("roomId") Long roomId,
            @Param("night") LocalDate night
    );

    default Optional<SeasonalRoomPrice> findBestForNight(Long roomId, LocalDate night) {
        List<SeasonalRoomPrice> matches = findCoveringNight(roomId, night);
        return matches.isEmpty() ? Optional.empty() : Optional.of(matches.getFirst());
    }
}
