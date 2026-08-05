package com.hotelbooking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelbooking.database.RoomStatus;
import com.hotelbooking.database.RoomType;
import com.hotelbooking.dto.RoomAvailabilityRequest;
import com.hotelbooking.dto.RoomImageRequest;
import com.hotelbooking.dto.RoomImageResponse;
import com.hotelbooking.dto.RoomPricingRequest;
import com.hotelbooking.dto.RoomRequest;
import com.hotelbooking.dto.RoomResponse;
import com.hotelbooking.exception.DuplicateRoomException;
import com.hotelbooking.exception.GlobalExceptionHandler;
import com.hotelbooking.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminRoomControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RoomService roomService;

    @InjectMocks
    private AdminRoomController adminRoomController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminRoomController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Test
    void createRoom_shouldReturn201() throws Exception {
        RoomRequest request = RoomRequest.builder()
                .roomNumber("101")
                .roomType(RoomType.STANDARD)
                .capacity(2)
                .pricePerNight(new BigDecimal("2500.00"))
                .build();
        RoomResponse response = RoomResponse.builder().id(1L).roomNumber("101").build();
        when(roomService.createRoom(any(RoomRequest.class))).thenReturn(response);

        mockMvc.perform(post("/admin/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roomNumber").value("101"));
    }

    @Test
    void createRoom_shouldReturn400WhenValidationFails() throws Exception {
        RoomRequest request = RoomRequest.builder()
                .roomNumber("")
                .capacity(0)
                .build();

        mockMvc.perform(post("/admin/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRoom_shouldReturn409WhenDuplicate() throws Exception {
        RoomRequest request = RoomRequest.builder()
                .roomNumber("101")
                .roomType(RoomType.STANDARD)
                .capacity(2)
                .pricePerNight(new BigDecimal("2500.00"))
                .build();
        when(roomService.createRoom(any())).thenThrow(new DuplicateRoomException("Room already exists with number: 101"));

        mockMvc.perform(post("/admin/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateAvailability_shouldReturn200() throws Exception {
        RoomAvailabilityRequest request = RoomAvailabilityRequest.builder().status(RoomStatus.RESERVED).build();
        RoomResponse response = RoomResponse.builder().id(1L).status(RoomStatus.RESERVED).build();
        when(roomService.updateAvailability(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/admin/rooms/1/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESERVED"));
    }

    @Test
    void updatePricing_shouldReturn200() throws Exception {
        RoomPricingRequest request = RoomPricingRequest.builder()
                .pricePerNight(new BigDecimal("2600"))
                .discountedPrice(new BigDecimal("2400"))
                .build();
        RoomResponse response = RoomResponse.builder().id(1L).pricePerNight(new BigDecimal("2600")).build();
        when(roomService.updatePricing(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/admin/rooms/1/pricing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pricePerNight").value(2600));
    }

    @Test
    void addRoomImage_shouldReturn201() throws Exception {
        RoomImageRequest request = RoomImageRequest.builder()
                .imageUrl("https://cdn.example.com/101.jpg")
                .primary(true)
                .build();
        RoomImageResponse response = RoomImageResponse.builder().id(5L).imageUrl(request.getImageUrl()).build();
        when(roomService.addRoomImage(eq(1L), any())).thenReturn(response);

        mockMvc.perform(post("/admin/rooms/1/images")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void deleteRoom_shouldReturn204() throws Exception {
        doNothing().when(roomService).deleteRoom(1L);

        mockMvc.perform(delete("/admin/rooms/1"))
                .andExpect(status().isNoContent());
    }
}
