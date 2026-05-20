package com.hotelbooking.repository;

import com.hotelbooking.database.RoomStatus;
import com.hotelbooking.database.RoomType;
import com.hotelbooking.entity.Room;
<<<<<<< HEAD
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

=======
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
>>>>>>> feature/module-1-foundation-practice
import java.util.List;
import java.util.Optional;

@Repository
<<<<<<< HEAD
public interface RoomRepository extends JpaRepository<Room, Long> {
=======
public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {
>>>>>>> feature/module-1-foundation-practice

    Optional<Room> findByRoomNumber(String roomNumber);

    boolean existsByRoomNumber(String roomNumber);

<<<<<<< HEAD
=======
    boolean existsByRoomNumberAndIdNot(String roomNumber, Long id);

>>>>>>> feature/module-1-foundation-practice
    List<Room> findByStatus(RoomStatus status);

    List<Room> findByRoomType(RoomType roomType);

    List<Room> findByRoomTypeAndStatus(RoomType roomType, RoomStatus status);
<<<<<<< HEAD
=======

    Page<Room> findByStatus(RoomStatus status, Pageable pageable);

    Page<Room> findByRoomType(RoomType roomType, Pageable pageable);

    Page<Room> findByCapacityGreaterThanEqual(Integer capacity, Pageable pageable);

    Page<Room> findByPricePerNightBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    @Query("""
            SELECT r FROM Room r
            WHERE (:roomNumber IS NULL OR LOWER(r.roomNumber) LIKE LOWER(CONCAT('%', :roomNumber, '%')))
              AND (:roomType IS NULL OR r.roomType = :roomType)
              AND (:status IS NULL OR r.status = :status)
              AND (:minCapacity IS NULL OR r.capacity >= :minCapacity)
              AND (:minPrice IS NULL OR r.pricePerNight >= :minPrice)
              AND (:maxPrice IS NULL OR r.pricePerNight <= :maxPrice)
            """)
    Page<Room> searchRooms(
            @Param("roomNumber") String roomNumber,
            @Param("roomType") RoomType roomType,
            @Param("status") RoomStatus status,
            @Param("minCapacity") Integer minCapacity,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );
>>>>>>> feature/module-1-foundation-practice
}
