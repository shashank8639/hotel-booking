package com.hotelbooking.repository;

import com.hotelbooking.database.RoomStatus;
import com.hotelbooking.database.RoomType;
import com.hotelbooking.entity.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {

    Optional<Room> findByIdAndDeletedFalse(Long id);

    Optional<Room> findByRoomNumber(String roomNumber);

    Optional<Room> findByRoomNumberAndDeletedFalse(String roomNumber);

    boolean existsByRoomNumber(String roomNumber);

    boolean existsByRoomNumberAndDeletedFalse(String roomNumber);

    boolean existsByRoomNumberAndIdNot(String roomNumber, Long id);

    boolean existsByRoomNumberAndDeletedFalseAndIdNot(String roomNumber, Long id);

    boolean existsByHotel_IdAndRoomNumberAndDeletedFalse(Long hotelId, String roomNumber);

    boolean existsByHotel_IdAndRoomNumberAndDeletedFalseAndIdNot(Long hotelId, String roomNumber, Long id);

    List<Room> findByHotel_IdAndDeletedFalse(Long hotelId);

    Page<Room> findByHotel_IdAndDeletedFalse(Long hotelId, Pageable pageable);

    List<Room> findByStatus(RoomStatus status);

    List<Room> findByStatusAndDeletedFalse(RoomStatus status);

    List<Room> findByRoomType(RoomType roomType);

    List<Room> findByRoomTypeAndStatus(RoomType roomType, RoomStatus status);

    Page<Room> findByDeletedFalse(Pageable pageable);

    Page<Room> findByStatus(RoomStatus status, Pageable pageable);

    Page<Room> findByRoomType(RoomType roomType, Pageable pageable);

    Page<Room> findByCapacityGreaterThanEqual(Integer capacity, Pageable pageable);

    Page<Room> findByPricePerNightBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    /**
     * Combined catalog search. Description uses case-insensitive keyword match
     * (backed by MySQL FULLTEXT index {@code ft_rooms_description} in production).
     */
    @Query("""
            SELECT r FROM Room r
            WHERE r.deleted = false
              AND (:roomNumber IS NULL OR LOWER(r.roomNumber) LIKE LOWER(CONCAT('%', :roomNumber, '%')))
              AND (:roomType IS NULL OR r.roomType = :roomType)
              AND (:status IS NULL OR r.status = :status)
              AND (:floorNumber IS NULL OR r.floorNumber = :floorNumber)
              AND (:minCapacity IS NULL OR r.capacity >= :minCapacity)
              AND (:minPrice IS NULL OR r.pricePerNight >= :minPrice)
              AND (:maxPrice IS NULL OR r.pricePerNight <= :maxPrice)
              AND (:description IS NULL OR LOWER(r.description) LIKE LOWER(CONCAT('%', :description, '%')))
            """)
    Page<Room> searchRooms(
            @Param("roomNumber") String roomNumber,
            @Param("roomType") RoomType roomType,
            @Param("status") RoomStatus status,
            @Param("floorNumber") Integer floorNumber,
            @Param("minCapacity") Integer minCapacity,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("description") String description,
            Pageable pageable
    );

    /**
     * Pessimistic write lock — serializes concurrent booking attempts on the same room row.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Room r WHERE r.id = :id AND r.deleted = false")
    Optional<Room> findByIdForUpdate(@Param("id") Long id);

    /**
     * Rooms that are operationally AVAILABLE and have no overlapping non-cancelled bookings.
     */
    @Query("""
            SELECT r FROM Room r
            WHERE r.deleted = false
              AND r.status = com.hotelbooking.database.RoomStatus.AVAILABLE
              AND r.id NOT IN (
                  SELECT br.room.id
                  FROM BookingRoom br
                  JOIN br.booking b
                  WHERE b.status <> com.hotelbooking.database.BookingStatus.CANCELLED
                    AND b.checkInDate < :checkOut
                    AND b.checkOutDate > :checkIn
              )
            """)
    List<Room> findAvailableRoomsForDates(
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );
}
