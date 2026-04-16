package com.hotelbooking.service;

import com.hotelbooking.dto.GuestRequest;
import com.hotelbooking.dto.GuestResponse;
import com.hotelbooking.entity.Guest;
import com.hotelbooking.exception.DuplicateGuestException;
import com.hotelbooking.exception.GuestHasBookingsException;
import com.hotelbooking.exception.GuestNotFoundException;
import com.hotelbooking.mapper.GuestMapper;
import com.hotelbooking.repository.BookingRepository;
import com.hotelbooking.repository.GuestRepository;
import com.hotelbooking.security.BookingOwnership;
import com.hotelbooking.service.impl.GuestServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuestServiceTest {

    @Mock
    private GuestRepository guestRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private GuestMapper guestMapper;

    @Mock
    private BookingOwnership bookingOwnership;

    @InjectMocks
    private GuestServiceImpl guestService;

    @BeforeEach
    void setUp() {
        lenient().doNothing().when(bookingOwnership).assertGuestEmailAllowed(anyString());
        lenient().doNothing().when(bookingOwnership).assertCanAccessGuest(anyLong());
    }

    @Test
    void createGuest_shouldSaveGuest() {
        GuestRequest request = GuestRequest.builder()
                .firstName("Amit")
                .lastName("Kumar")
                .email("amit.kumar@example.com")
                .phone("+91-9000000000")
                .build();

        Guest entity = Guest.builder().firstName("Amit").lastName("Kumar").email("amit.kumar@example.com").build();
        Guest saved = Guest.builder().firstName("Amit").lastName("Kumar").email("amit.kumar@example.com").build();
        saved.setId(1L);
        GuestResponse response = GuestResponse.builder().id(1L).email("amit.kumar@example.com").build();

        when(guestRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(guestRepository.existsByPhone(request.getPhone())).thenReturn(false);
        when(guestMapper.toEntity(request)).thenReturn(entity);
        when(guestRepository.save(entity)).thenReturn(saved);
        when(guestMapper.toResponse(saved)).thenReturn(response);

        GuestResponse result = guestService.createGuest(request);

        assertThat(result.getId()).isEqualTo(1L);
        verify(guestRepository).save(entity);
    }

    /**
     * Mockito practice #1 — ArgumentCaptor inspects the Guest entity passed to save().
     */
    @Test
    void createGuest_shouldPersistMappedGuest_capturedWithArgumentCaptor() {
        GuestRequest request = GuestRequest.builder()
                .firstName("Neha")
                .lastName("Gupta")
                .email("neha.practice@example.com")
                .phone("+91-9111111111")
                .build();

        Guest entity = Guest.builder()
                .firstName("Neha")
                .lastName("Gupta")
                .email("neha.practice@example.com")
                .phone("+91-9111111111")
                .build();
        Guest saved = Guest.builder()
                .firstName("Neha")
                .lastName("Gupta")
                .email("neha.practice@example.com")
                .phone("+91-9111111111")
                .build();
        saved.setId(42L);
        GuestResponse response = GuestResponse.builder().id(42L).email("neha.practice@example.com").build();

        when(guestRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(guestRepository.existsByPhone(request.getPhone())).thenReturn(false);
        when(guestMapper.toEntity(request)).thenReturn(entity);
        when(guestRepository.save(any(Guest.class))).thenReturn(saved);
        when(guestMapper.toResponse(saved)).thenReturn(response);

        guestService.createGuest(request);

        ArgumentCaptor<Guest> guestCaptor = ArgumentCaptor.forClass(Guest.class);
        verify(guestRepository).save(guestCaptor.capture());
        assertThat(guestCaptor.getValue().getEmail()).isEqualTo("neha.practice@example.com");
        assertThat(guestCaptor.getValue().getFirstName()).isEqualTo("Neha");
    }

    @Test
    void createGuest_shouldThrowWhenEmailExists() {
        GuestRequest request = GuestRequest.builder()
                .firstName("Amit")
                .lastName("Kumar")
                .email("amit.kumar@example.com")
                .build();

        when(guestRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> guestService.createGuest(request))
                .isInstanceOf(DuplicateGuestException.class);
    }

    @Test
    void updateGuest_shouldUpdateExistingGuest() {
        Long id = 1L;
        GuestRequest request = GuestRequest.builder()
                .firstName("Amit")
                .lastName("Singh")
                .email("amit.kumar@example.com")
                .build();
        Guest existing = Guest.builder().firstName("Amit").lastName("Kumar").email("amit.kumar@example.com").build();
        existing.setId(id);
        GuestResponse response = GuestResponse.builder().id(id).lastName("Singh").build();

        when(guestRepository.findById(id)).thenReturn(Optional.of(existing));
        when(guestRepository.existsByEmailAndIdNot(request.getEmail(), id)).thenReturn(false);
        when(guestRepository.save(existing)).thenReturn(existing);
        when(guestMapper.toResponse(existing)).thenReturn(response);

        GuestResponse result = guestService.updateGuest(id, request);

        assertThat(result.getLastName()).isEqualTo("Singh");
        verify(guestMapper).updateEntityFromRequest(request, existing);
    }

    @Test
    void deleteGuest_shouldDeleteWhenNoBookings() {
        Long id = 1L;
        Guest guest = Guest.builder().email("amit.kumar@example.com").build();
        guest.setId(id);

        when(guestRepository.findById(id)).thenReturn(Optional.of(guest));
        when(bookingRepository.countAllRowsByGuestId(id)).thenReturn(0L);

        guestService.deleteGuest(id);

        verify(guestRepository).delete(guest);
    }

    @Test
    void deleteGuest_shouldThrowWhenBookingsExist() {
        Long id = 1L;
        Guest guest = Guest.builder().email("amit.kumar@example.com").build();
        guest.setId(id);

        when(guestRepository.findById(id)).thenReturn(Optional.of(guest));
        when(bookingRepository.countAllRowsByGuestId(id)).thenReturn(1L);

        assertThatThrownBy(() -> guestService.deleteGuest(id))
                .isInstanceOf(GuestHasBookingsException.class);

        verify(guestRepository, never()).delete(any());
    }

    @Test
    void searchByEmail_shouldReturnGuest() {
        Guest guest = Guest.builder().email("rahul.sharma@example.com").build();
        guest.setId(1L);
        GuestResponse response = GuestResponse.builder().id(1L).email("rahul.sharma@example.com").build();

        when(guestRepository.findByEmailIgnoreCase("rahul.sharma@example.com")).thenReturn(Optional.of(guest));
        when(guestMapper.toResponse(guest)).thenReturn(response);

        GuestResponse result = guestService.searchByEmail("rahul.sharma@example.com");

        assertThat(result.getEmail()).isEqualTo("rahul.sharma@example.com");
    }

    @Test
    void getGuestById_shouldThrowWhenMissing() {
        when(guestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> guestService.getGuestById(99L))
                .isInstanceOf(GuestNotFoundException.class);
    }

    @Test
    void getAllGuests_shouldReturnPagedResults() {
        Guest guest = Guest.builder().email("rahul.sharma@example.com").build();
        guest.setId(1L);
        GuestResponse response = GuestResponse.builder().id(1L).build();
        PageRequest pageable = PageRequest.of(0, 10);

        when(guestRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(guest)));
        when(guestMapper.toResponse(guest)).thenReturn(response);

        var page = guestService.getAllGuests(pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
    }
}
