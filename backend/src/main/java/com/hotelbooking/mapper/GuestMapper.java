package com.hotelbooking.mapper;

import com.hotelbooking.dto.GuestRequest;
import com.hotelbooking.dto.GuestResponse;
import com.hotelbooking.entity.Guest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface GuestMapper {

    @Mapping(target = "bookings", ignore = true)
    Guest toEntity(GuestRequest request);

    GuestResponse toResponse(Guest guest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "bookings", ignore = true)
    void updateEntityFromRequest(GuestRequest request, @MappingTarget Guest guest);
}
