package com.hotelbooking.service;

import com.hotelbooking.database.RoomStatus;
import com.hotelbooking.database.RoomType;
import com.hotelbooking.dto.RoomAvailabilityRequest;
import com.hotelbooking.dto.RoomDescriptionPatchRequest;
import com.hotelbooking.dto.RoomImageRequest;
import com.hotelbooking.dto.RoomImageResponse;
import com.hotelbooking.dto.RoomPricingRequest;
import com.hotelbooking.dto.RoomRequest;
import com.hotelbooking.dto.RoomResponse;
import com.hotelbooking.entity.Hotel;
import com.hotelbooking.entity.Room;
import com.hotelbooking.entity.RoomImage;
import com.hotelbooking.exception.DuplicateRoomException;
import com.hotelbooking.exception.InvalidRoomPricingException;
import com.hotelbooking.exception.InvalidRoomStatusTransitionException;
import com.hotelbooking.exception.RoomNotFoundException;
import com.hotelbooking.mapper.RoomMapper;
import com.hotelbooking.repository.BookingRepository;
import com.hotelbooking.repository.HotelRepository;
import com.hotelbooking.repository.RoomImageRepository;
import com.hotelbooking.repository.RoomRepository;
import com.hotelbooking.service.impl.RoomServiceImpl;
import com.hotelbooking.util.HotelTestSupport;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
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
    private BookingRepository bookingRepository;

    @Mock
    private RoomMapper roomMapper;

    @Mock
    private HotelRepository hotelRepository;

    @InjectMocks
    private RoomServiceImpl roomService;

    private Hotel hotel;

    @BeforeEach
    void setUp() {
        hotel = HotelTestSupport.sampleHotel(1L);
        lenient().when(hotelRepository.findBySlug(HotelTestSupport.DEFAULT_HOTEL_SLUG)).thenReturn(Optional.of(hotel));
        lenient().when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
    }

    @Test
    void createRoom_shouldRejectDuplicateRoomNumber() {
        RoomRequest request = RoomRequest.builder()
                .roomNumber("101")
                .roomType(RoomType.STANDARD)
                .capacity(2)
                .pricePerNight(new BigDecimal("2500"))
                .build();

        when(roomRepository.existsByHotel_IdAndRoomNumberAndDeletedFalse(1L, "101")).thenReturn(true);

        assertThatThrownBy(() -> roomService.createRoom(request))
                .isInstanceOf(DuplicateRoomException.class);
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void createRoom_shouldPersistMappedRoom() {
        RoomRequest request = RoomRequest.builder()
                .roomNumber("101")
                .roomType(RoomType.STANDARD)
                .capacity(2)
                .pricePerNight(new BigDecimal("2500"))
                .build();
        Room entity = Room.builder().roomNumber("101").build();
        Room saved = Room.builder().roomNumber("101").hotel(hotel).build();
        saved.setId(1L);
        RoomResponse response = RoomResponse.builder().id(1L).roomNumber("101").build();

        when(roomRepository.existsByHotel_IdAndRoomNumberAndDeletedFalse(1L, "101")).thenReturn(false);
        when(roomMapper.toEntity(request)).thenReturn(entity);
        when(roomRepository.save(entity)).thenReturn(saved);
        when(roomMapper.toResponse(saved)).thenReturn(response);

        RoomResponse result = roomService.createRoom(request);

        assertThat(result.getId()).isEqualTo(1L);
        verify(roomRepository).save(entity);
        assertThat(entity.getHotel()).isEqualTo(hotel);
    }

    @Test
    void updatePricing_shouldRejectDiscountAboveBase() {
        Room room = Room.builder()
                .roomNumber("101")
                .pricePerNight(new BigDecimal("2500"))
                .status(RoomStatus.AVAILABLE)
                .build();
        room.setId(1L);
        when(roomRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(room));

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
        when(roomRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(room));

        RoomAvailabilityRequest request = RoomAvailabilityRequest.builder()
                .status(RoomStatus.OCCUPIED)
                .build();

        assertThatThrownBy(() -> roomService.updateAvailability(1L, request))
                .isInstanceOf(InvalidRoomStatusTransitionException.class);
    }

    @Test
    void updateAvailability_shouldAllowCleaningAfterOccupied() {
        Room room = Room.builder().roomNumber("101").status(RoomStatus.OCCUPIED).build();
        room.setId(1L);
        RoomResponse response = RoomResponse.builder().id(1L).status(RoomStatus.CLEANING).build();

        when(roomRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(room));
        when(roomRepository.save(room)).thenReturn(room);
        when(roomMapper.toResponse(room)).thenReturn(response);

        RoomResponse result = roomService.updateAvailability(
                1L,
                RoomAvailabilityRequest.builder().status(RoomStatus.CLEANING).build()
        );

        assertThat(result.getStatus()).isEqualTo(RoomStatus.CLEANING);
        assertThat(room.getStatus()).isEqualTo(RoomStatus.CLEANING);
    }

    @Test
    void updateAvailability_shouldAllowValidTransition() {
        Room room = Room.builder().roomNumber("101").status(RoomStatus.AVAILABLE).build();
        room.setId(1L);
        RoomResponse response = RoomResponse.builder().id(1L).status(RoomStatus.RESERVED).build();

        when(roomRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(room));
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
    void deleteRoom_shouldSoftDelete() {
        Room room = Room.builder().roomNumber("101").deleted(false).build();
        room.setId(1L);
        when(roomRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(room));
        when(roomRepository.save(room)).thenReturn(room);

        roomService.deleteRoom(1L);

        assertThat(room.isDeleted()).isTrue();
        verify(roomRepository).save(room);
        verify(roomRepository, never()).delete(any(Room.class));
    }

    @Test
    void patchDescription_shouldUpdateOnlyDescription() {
        Room room = Room.builder().roomNumber("101").description("old").build();
        room.setId(1L);
        RoomResponse response = RoomResponse.builder().id(1L).description("sea view").build();

        when(roomRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(room));
        when(roomRepository.save(room)).thenReturn(room);
        when(roomMapper.toResponse(room)).thenReturn(response);

        RoomResponse result = roomService.patchDescription(
                1L,
                RoomDescriptionPatchRequest.builder().description("sea view").build()
        );

        assertThat(room.getDescription()).isEqualTo("sea view");
        assertThat(result.getDescription()).isEqualTo("sea view");
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

        when(roomRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(room));
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
        when(roomRepository.findByIdAndDeletedFalse(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> roomService.getRoomById(99L))
                .isInstanceOf(RoomNotFoundException.class);
    }

    @Test
    void searchRooms_shouldMapPagedResults() {
        Room room = Room.builder().roomNumber("101").build();
        room.setId(1L);
        RoomResponse response = RoomResponse.builder().id(1L).roomNumber("101").build();
        PageRequest pageable = PageRequest.of(0, 10);

        when(roomRepository.searchRooms(
                isNull(), eq(RoomType.STANDARD), eq(RoomStatus.AVAILABLE), isNull(),
                eq(2), eq(new BigDecimal("1000")), eq(new BigDecimal("3000")), isNull(), any()
        )).thenReturn(new PageImpl<>(List.of(room)));
        when(roomMapper.toResponse(room)).thenReturn(response);

        var page = roomService.searchRooms(null, RoomType.STANDARD, RoomStatus.AVAILABLE, null, 2,
                new BigDecimal("1000"), new BigDecimal("3000"), null, pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getAllRooms_shouldCapPageSizeAt50() {
        when(roomRepository.findByDeletedFalse(any())).thenReturn(new PageImpl<>(List.of()));

        roomService.getAllRooms(PageRequest.of(0, 200));

        verify(roomRepository).findByDeletedFalse(argThat(p -> p.getPageSize() == 50));
    }
}
