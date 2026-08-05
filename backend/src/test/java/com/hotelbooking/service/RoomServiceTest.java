package com.hotelbooking.service;

import com.hotelbooking.database.RoomStatus;
import com.hotelbooking.database.RoomType;
import com.hotelbooking.dto.RoomAvailabilityRequest;
import com.hotelbooking.dto.RoomImageRequest;
import com.hotelbooking.dto.RoomImageResponse;
import com.hotelbooking.dto.RoomPricingRequest;
import com.hotelbooking.dto.RoomRequest;
import com.hotelbooking.dto.RoomResponse;
import com.hotelbooking.entity.BookingRoom;
import com.hotelbooking.entity.Room;
import com.hotelbooking.entity.RoomImage;
import com.hotelbooking.exception.DuplicateRoomException;
import com.hotelbooking.exception.InvalidRoomPricingException;
import com.hotelbooking.exception.InvalidRoomStatusTransitionException;
import com.hotelbooking.exception.RoomHasBookingsException;
import com.hotelbooking.exception.RoomNotFoundException;
import com.hotelbooking.mapper.RoomMapper;
import com.hotelbooking.repository.BookingRoomRepository;
import com.hotelbooking.repository.RoomImageRepository;
import com.hotelbooking.repository.RoomRepository;
import com.hotelbooking.service.impl.RoomServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomImageRepository roomImageRepository;

    @Mock
    private BookingRoomRepository bookingRoomRepository;

    @Mock
    private RoomMapper roomMapper;

    @InjectMocks
    private RoomServiceImpl roomService;

    @Test
    void createRoom_shouldSaveRoom() {
        RoomRequest request = RoomRequest.builder()
                .roomNumber("101")
                .roomType(RoomType.STANDARD)
                .capacity(2)
                .pricePerNight(new BigDecimal("2500.00"))
                .build();
        Room entity = Room.builder().roomNumber("101").build();
        Room saved = Room.builder().roomNumber("101").pricePerNight(new BigDecimal("2500.00")).build();
        saved.setId(1L);
        RoomResponse response = RoomResponse.builder().id(1L).roomNumber("101").build();

        when(roomRepository.existsByRoomNumber("101")).thenReturn(false);
        when(roomMapper.toEntity(request)).thenReturn(entity);
        when(roomRepository.save(entity)).thenReturn(saved);
        when(roomMapper.toResponse(saved)).thenReturn(response);

        RoomResponse result = roomService.createRoom(request);

        assertThat(result.getId()).isEqualTo(1L);
        verify(roomRepository).save(entity);
    }

    @Test
    void createRoom_shouldRejectDuplicateRoomNumber() {
        RoomRequest request = RoomRequest.builder()
                .roomNumber("101")
                .roomType(RoomType.STANDARD)
                .capacity(2)
                .pricePerNight(new BigDecimal("2500.00"))
                .build();
        when(roomRepository.existsByRoomNumber("101")).thenReturn(true);

        assertThatThrownBy(() -> roomService.createRoom(request))
                .isInstanceOf(DuplicateRoomException.class);
    }

    @Test
    void updatePricing_shouldRejectDiscountAboveBase() {
        Room room = Room.builder()
                .roomNumber("101")
                .pricePerNight(new BigDecimal("2500"))
                .status(RoomStatus.AVAILABLE)
                .build();
        room.setId(1L);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        RoomPricingRequest request = RoomPricingRequest.builder()
                .pricePerNight(new BigDecimal("2500"))
                .discountedPrice(new BigDecimal("3000"))
                .build();

        assertThatThrownBy(() -> roomService.updatePricing(1L, request))
                .isInstanceOf(InvalidRoomPricingException.class);
    }

    @Test
    void updateAvailability_shouldRejectInvalidTransition() {
        Room room = Room.builder().roomNumber("101").status(RoomStatus.AVAILABLE).build();
        room.setId(1L);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        RoomAvailabilityRequest request = RoomAvailabilityRequest.builder()
                .status(RoomStatus.OCCUPIED)
                .build();

        assertThatThrownBy(() -> roomService.updateAvailability(1L, request))
                .isInstanceOf(InvalidRoomStatusTransitionException.class);
    }

    @Test
    void updateAvailability_shouldAllowValidTransition() {
        Room room = Room.builder().roomNumber("101").status(RoomStatus.AVAILABLE).build();
        room.setId(1L);
        RoomResponse response = RoomResponse.builder().id(1L).status(RoomStatus.RESERVED).build();

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomRepository.save(room)).thenReturn(room);
        when(roomMapper.toResponse(room)).thenReturn(response);

        RoomResponse result = roomService.updateAvailability(
                1L,
                RoomAvailabilityRequest.builder().status(RoomStatus.RESERVED).build()
        );

        assertThat(result.getStatus()).isEqualTo(RoomStatus.RESERVED);
        assertThat(room.getStatus()).isEqualTo(RoomStatus.RESERVED);
    }

    @Test
    void deleteRoom_shouldBlockWhenBookingsExist() {
        Room room = Room.builder().roomNumber("101").build();
        room.setId(1L);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(bookingRoomRepository.findByRoomId(1L)).thenReturn(List.of(new BookingRoom()));

        assertThatThrownBy(() -> roomService.deleteRoom(1L))
                .isInstanceOf(RoomHasBookingsException.class);
        verify(roomRepository, never()).delete(any(Room.class));
    }

    @Test
    void addRoomImage_shouldPersistImage() {
        Room room = Room.builder().roomNumber("101").images(new ArrayList<>()).build();
        room.setId(1L);
        RoomImageRequest request = RoomImageRequest.builder()
                .imageUrl("https://cdn.example.com/101.jpg")
                .primary(true)
                .build();
        RoomImage image = RoomImage.builder().imageUrl(request.getImageUrl()).primary(true).displayOrder(0).build();
        RoomImageResponse imageResponse = RoomImageResponse.builder().id(10L).imageUrl(request.getImageUrl()).build();

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomMapper.toImageEntity(request)).thenReturn(image);
        when(roomImageRepository.save(image)).thenAnswer(invocation -> {
            RoomImage saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });
        when(roomMapper.toImageResponse(any(RoomImage.class))).thenReturn(imageResponse);

        RoomImageResponse result = roomService.addRoomImage(1L, request);

        assertThat(result.getId()).isEqualTo(10L);
        verify(roomImageRepository).save(image);
    }

    @Test
    void getRoomById_shouldThrowWhenMissing() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> roomService.getRoomById(99L))
                .isInstanceOf(RoomNotFoundException.class);
    }

    @Test
    void searchRooms_shouldMapPagedResults() {
        Room room = Room.builder().roomNumber("101").build();
        room.setId(1L);
        RoomResponse response = RoomResponse.builder().id(1L).roomNumber("101").build();
        PageRequest pageable = PageRequest.of(0, 10);

        when(roomRepository.searchRooms(null, RoomType.STANDARD, RoomStatus.AVAILABLE, 2,
                new BigDecimal("1000"), new BigDecimal("3000"), pageable))
                .thenReturn(new PageImpl<>(List.of(room)));
        when(roomMapper.toResponse(room)).thenReturn(response);

        var page = roomService.searchRooms(null, RoomType.STANDARD, RoomStatus.AVAILABLE, 2,
                new BigDecimal("1000"), new BigDecimal("3000"), pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
    }
}
