package com.hotelbooking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelbooking.database.RoomStatus;
import com.hotelbooking.database.RoomType;
import com.hotelbooking.dto.RoomResponse;
import com.hotelbooking.exception.GlobalExceptionHandler;
import com.hotelbooking.exception.RoomNotFoundException;
import com.hotelbooking.service.RoomService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RoomControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RoomService roomService;

    @InjectMocks
    private RoomController roomController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(roomController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void getAllRooms_shouldReturnPagedResults() throws Exception {
        RoomResponse response = RoomResponse.builder().id(1L).roomNumber("101").build();
        when(roomService.getAllRooms(any())).thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].roomNumber").value("101"));
    }

    @Test
    void getRoomById_shouldReturn200() throws Exception {
        when(roomService.getRoomById(1L)).thenReturn(RoomResponse.builder().id(1L).roomNumber("101").build());

        mockMvc.perform(get("/rooms/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomNumber").value("101"));
    }

    @Test
    void getRoomById_shouldReturn404() throws Exception {
        when(roomService.getRoomById(99L)).thenThrow(new RoomNotFoundException(99L));

        mockMvc.perform(get("/rooms/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchRooms_shouldReturnFilteredPage() throws Exception {
        RoomResponse response = RoomResponse.builder()
                .id(1L)
                .roomNumber("101")
                .roomType(RoomType.STANDARD)
                .status(RoomStatus.AVAILABLE)
                .build();

        when(roomService.searchRooms(
                nullable(String.class),
                nullable(RoomType.class),
                nullable(RoomStatus.class),
                nullable(Integer.class),
                nullable(Integer.class),
                nullable(BigDecimal.class),
                nullable(BigDecimal.class),
                nullable(String.class),
                any()
        )).thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/rooms/search")
                        .param("roomType", "STANDARD")
                        .param("status", "AVAILABLE")
                        .param("floorNumber", "1")
                        .param("description", "city")
                        .param("minCapacity", "2")
                        .param("minPrice", "1000")
                        .param("maxPrice", "3000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].roomNumber").value("101"));
    }

    @Test
    void getRoomTypes_shouldReturnEnumValues() throws Exception {
        when(roomService.getRoomTypes()).thenReturn(List.of(RoomType.STANDARD, RoomType.DELUXE));

        mockMvc.perform(get("/rooms/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("STANDARD"));
    }
}
