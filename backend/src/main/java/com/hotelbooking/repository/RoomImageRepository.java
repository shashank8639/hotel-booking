package com.hotelbooking.repository;

import com.hotelbooking.entity.RoomImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomImageRepository extends JpaRepository<RoomImage, Long> {

    List<RoomImage> findByRoomIdOrderByDisplayOrderAsc(Long roomId);

    Optional<RoomImage> findByIdAndRoomId(Long id, Long roomId);

    void deleteByIdAndRoomId(Long id, Long roomId);

    long countByRoomId(Long roomId);
}
