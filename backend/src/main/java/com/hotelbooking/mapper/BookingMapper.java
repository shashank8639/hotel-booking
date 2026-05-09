package com.hotelbooking.mapper;

import com.hotelbooking.dto.BookingResponse;
import com.hotelbooking.dto.BookingRoomResponse;
import com.hotelbooking.entity.Booking;
import com.hotelbooking.entity.BookingRoom;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "guestId", source = "guest.id")
    @Mapping(target = "guestFirstName", source = "guest.firstName")
    @Mapping(target = "guestLastName", source = "guest.lastName")
    @Mapping(target = "guestEmail", source = "guest.email")
    @Mapping(target = "numberOfNights", expression = "java(calculateNights(booking))")
    @Mapping(target = "rooms", source = "bookingRooms")
    BookingResponse toResponse(Booking booking);

    List<BookingResponse> toResponseList(List<Booking> bookings);

    @Mapping(target = "roomId", source = "room.id")
    @Mapping(target = "roomNumber", source = "room.roomNumber")
    BookingRoomResponse toRoomResponse(BookingRoom bookingRoom);

    List<BookingRoomResponse> toRoomResponseList(List<BookingRoom> bookingRooms);

    default Integer calculateNights(Booking booking) {
        if (booking.getCheckInDate() == null || booking.getCheckOutDate() == null) {
            return null;
        }
        return (int) ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
    }
}
