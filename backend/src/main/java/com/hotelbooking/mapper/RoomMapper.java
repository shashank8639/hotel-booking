package com.hotelbooking.mapper;

import com.hotelbooking.dto.RoomImageRequest;
import com.hotelbooking.dto.RoomImageResponse;
import com.hotelbooking.dto.RoomRequest;
import com.hotelbooking.dto.RoomResponse;
import com.hotelbooking.entity.Room;
import com.hotelbooking.entity.RoomImage;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    @Mapping(target = "bookingRooms", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "seasonalPrices", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "hotel", ignore = true)
    Room toEntity(RoomRequest request);

    @Mapping(target = "effectivePrice", expression = "java(room.getEffectivePrice())")
    @Mapping(target = "images", source = "images")
    @Mapping(target = "hotelId", source = "hotel.id")
    @Mapping(target = "hotelName", source = "hotel.name")
    @Mapping(target = "hotelSlug", source = "hotel.slug")
    RoomResponse toResponse(Room room);

    List<RoomResponse> toResponseList(List<Room> rooms);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "bookingRooms", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "seasonalPrices", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "hotel", ignore = true)
    void updateEntityFromRequest(RoomRequest request, @MappingTarget Room room);

    @Mapping(target = "room", ignore = true)
    RoomImage toImageEntity(RoomImageRequest request);

    @Mapping(target = "roomId", source = "room.id")
    RoomImageResponse toImageResponse(RoomImage image);

    List<RoomImageResponse> toImageResponseList(List<RoomImage> images);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "room", ignore = true)
    void updateImageFromRequest(RoomImageRequest request, @MappingTarget RoomImage image);

    @AfterMapping
    default void defaultStatus(RoomRequest request, @MappingTarget Room room) {
        if (room.getStatus() == null) {
            room.setStatus(com.hotelbooking.database.RoomStatus.AVAILABLE);
        }
        if (room.getCurrency() == null || room.getCurrency().isBlank()) {
            room.setCurrency("INR");
        }
    }
}
