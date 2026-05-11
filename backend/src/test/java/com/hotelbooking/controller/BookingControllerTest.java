package com.hotelbooking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hotelbooking.database.BookingStatus;
import com.hotelbooking.dto.AvailabilityResponse;
import com.hotelbooking.dto.BookingRequest;
import com.hotelbooking.dto.BookingResponse;
import com.hotelbooking.dto.BookingStatusRequest;
import com.hotelbooking.dto.RoomAvailabilityItem;
import com.hotelbooking.exception.GlobalExceptionHandler;
import com.hotelbooking.exception.InvalidBookingDatesException;
import com.hotelbooking.exception.RoomAlreadyBookedException;
import com.hotelbooking.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingController bookingController;

    private LocalDate checkIn;
    private LocalDate checkOut;

    @BeforeEach
    void setUp() {
        checkIn = LocalDate.now().plusDays(10);
        checkOut = checkIn.plusDays(2);

        mockMvc = MockMvcBuilders.standaloneSetup(bookingController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void createBooking_shouldReturn201() throws Exception {
        BookingRequest request = BookingRequest.builder()
                .guestId(1L)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .roomIds(List.of(10L))
                .build();
        when(bookingService.createBooking(any(BookingRequest.class))).thenReturn(
                BookingResponse.builder()
                        .id(100L)
                        .guestId(1L)
                        .status(BookingStatus.PENDING)
                        .totalAmount(new BigDecimal("5000.00"))
                        .build()
        );

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void createBooking_shouldReturn400WhenGuestIdMissing() throws Exception {
        BookingRequest request = BookingRequest.builder()
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .roomIds(List.of(10L))
                .build();

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_shouldReturn409OnOverlap() throws Exception {
        BookingRequest request = BookingRequest.builder()
                .guestId(1L)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .roomIds(List.of(10L))
                .build();
        when(bookingService.createBooking(any())).thenThrow(new RoomAlreadyBookedException(10L, "101"));

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getBookingById_shouldReturn200() throws Exception {
        when(bookingService.getBookingById(100L)).thenReturn(
                BookingResponse.builder().id(100L).status(BookingStatus.CONFIRMED).build()
        );

        mockMvc.perform(get("/bookings/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void getAllBookings_shouldReturnPage() throws Exception {
        when(bookingService.getAllBookings(any())).thenReturn(
                new PageImpl<>(
                        List.of(BookingResponse.builder().id(1L).build()),
                        PageRequest.of(0, 10),
                        1
                )
        );

        mockMvc.perform(get("/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void getBookingsByGuest_shouldReturnPage() throws Exception {
        when(bookingService.getBookingsByGuest(eq(1L), isNull(), isNull(), isNull(), any())).thenReturn(
                new PageImpl<>(List.of(BookingResponse.builder().id(2L).build()), PageRequest.of(0, 10), 1)
        );

        mockMvc.perform(get("/bookings/guest/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(2));
    }

    @Test
    void getBookingsByGuest_shouldPassFilters() throws Exception {
        when(bookingService.getBookingsByGuest(
                eq(1L), eq(BookingStatus.CONFIRMED), eq(checkIn), eq(checkOut), any()
        )).thenReturn(new PageImpl<>(List.of(BookingResponse.builder().id(9L).build()), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/bookings/guest/1")
                        .param("status", "CONFIRMED")
                        .param("from", checkIn.toString())
                        .param("to", checkOut.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(9));
    }

    @Test
    void getBookingsByStatus_shouldReturnPage() throws Exception {
        when(bookingService.getBookingsByStatus(eq(BookingStatus.PENDING), any())).thenReturn(
                new PageImpl<>(List.of(BookingResponse.builder().id(3L).build()), PageRequest.of(0, 10), 1)
        );

        mockMvc.perform(get("/bookings/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(3));
    }

    @Test
    void cancelBooking_shouldReturn200() throws Exception {
        when(bookingService.cancelBooking(5L)).thenReturn(
                BookingResponse.builder().id(5L).status(BookingStatus.CANCELLED).build()
        );

        mockMvc.perform(put("/bookings/5/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void updateStatus_shouldReturn200() throws Exception {
        BookingStatusRequest body = BookingStatusRequest.builder().status(BookingStatus.CONFIRMED).build();
        when(bookingService.updateBookingStatus(eq(5L), any())).thenReturn(
                BookingResponse.builder().id(5L).status(BookingStatus.CONFIRMED).build()
        );

        mockMvc.perform(put("/bookings/5/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void checkAvailability_shouldReturn200() throws Exception {
        when(bookingService.checkAvailability(eq(checkIn), eq(checkOut), isNull(), isNull())).thenReturn(
                AvailabilityResponse.builder()
                        .checkInDate(checkIn)
                        .checkOutDate(checkOut)
                        .numberOfNights(2)
                        .rooms(List.of(RoomAvailabilityItem.builder()
                                .roomId(10L)
                                .available(true)
                                .build()))
                        .build()
        );

        mockMvc.perform(get("/bookings/availability")
                        .param("checkInDate", checkIn.toString())
                        .param("checkOutDate", checkOut.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfNights").value(2))
                .andExpect(jsonPath("$.rooms[0].available").value(true));
    }

    @Test
    void checkAvailability_shouldReturn400OnInvalidDates() throws Exception {
        when(bookingService.checkAvailability(any(), any(), any(), any()))
                .thenThrow(new InvalidBookingDatesException("Check-out date must be after check-in date"));

        mockMvc.perform(get("/bookings/availability")
                        .param("checkInDate", checkOut.toString())
                        .param("checkOutDate", checkIn.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void availabilityMatrix_shouldReturn200() throws Exception {
        when(bookingService.getAvailabilityMatrix(any(), eq(30))).thenReturn(
                com.hotelbooking.dto.RoomAvailabilityMatrixResponse.builder()
                        .from(checkIn)
                        .to(checkIn.plusDays(30))
                        .dayCount(30)
                        .dates(List.of())
                        .rooms(List.of())
                        .build()
        );

        mockMvc.perform(get("/bookings/availability-matrix")
                        .param("from", checkIn.toString())
                        .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dayCount").value(30));
    }
}
