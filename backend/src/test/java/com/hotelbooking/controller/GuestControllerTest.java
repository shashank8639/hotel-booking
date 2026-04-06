package com.hotelbooking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelbooking.dto.GuestRequest;
import com.hotelbooking.dto.GuestResponse;
import com.hotelbooking.exception.DuplicateGuestException;
import com.hotelbooking.exception.GlobalExceptionHandler;
import com.hotelbooking.exception.GuestNotFoundException;
import com.hotelbooking.service.GuestService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GuestControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private GuestService guestService;

    @InjectMocks
    private GuestController guestController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(guestController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void createGuest_shouldReturn201() throws Exception {
        GuestRequest request = GuestRequest.builder()
                .firstName("Rahul")
                .lastName("Sharma")
                .email("rahul.sharma@example.com")
                .phone("+91-9876543210")
                .build();

        GuestResponse response = GuestResponse.builder()
                .id(1L)
                .firstName("Rahul")
                .lastName("Sharma")
                .email("rahul.sharma@example.com")
                .build();

        when(guestService.createGuest(any(GuestRequest.class))).thenReturn(response);

        mockMvc.perform(post("/guests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("rahul.sharma@example.com"));
    }

    @Test
    void createGuest_shouldReturn400WhenValidationFails() throws Exception {
        GuestRequest request = GuestRequest.builder()
                .firstName("")
                .lastName("Sharma")
                .email("invalid-email")
                .build();

        mockMvc.perform(post("/guests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.email").exists());
    }

    /**
     * Practice #1 — blank email must fail Bean Validation before the service runs.
     * Proves: 400 + validationErrors.email map (GlobalExceptionHandler).
     */
    @Test
    void createGuest_blankEmail_returns400WithFieldMap() throws Exception {
        GuestRequest request = GuestRequest.builder()
                .firstName("Rahul")
                .lastName("Sharma")
                .email("")
                .phone("+91-9876543210")
                .build();

        mockMvc.perform(post("/guests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.email").exists());
    }

    @Test
    void getGuestById_shouldReturn200() throws Exception {
        GuestResponse response = GuestResponse.builder().id(1L).email("rahul.sharma@example.com").build();
        when(guestService.getGuestById(1L)).thenReturn(response);

        mockMvc.perform(get("/guests/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getGuestById_shouldReturn404WhenNotFound() throws Exception {
        when(guestService.getGuestById(99L)).thenThrow(new GuestNotFoundException(99L));

        mockMvc.perform(get("/guests/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllGuests_shouldReturnPagedResults() throws Exception {
        GuestResponse response = GuestResponse.builder().id(1L).lastName("Sharma").build();
        when(guestService.getAllGuests(any())).thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/guests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void updateGuest_shouldReturn200() throws Exception {
        GuestRequest request = GuestRequest.builder()
                .firstName("Rahul")
                .lastName("Verma")
                .email("rahul.sharma@example.com")
                .build();
        GuestResponse response = GuestResponse.builder().id(1L).lastName("Verma").build();

        when(guestService.updateGuest(eq(1L), any(GuestRequest.class))).thenReturn(response);

        mockMvc.perform(put("/guests/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Verma"));
    }

    @Test
    void deleteGuest_shouldReturn204() throws Exception {
        doNothing().when(guestService).deleteGuest(1L);

        mockMvc.perform(delete("/guests/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void createGuest_shouldReturn409WhenDuplicate() throws Exception {
        GuestRequest request = GuestRequest.builder()
                .firstName("Rahul")
                .lastName("Sharma")
                .email("rahul.sharma@example.com")
                .build();

        when(guestService.createGuest(any(GuestRequest.class)))
                .thenThrow(new DuplicateGuestException("Guest already exists with email: rahul.sharma@example.com"));

        mockMvc.perform(post("/guests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void searchByEmail_shouldReturnGuest() throws Exception {
        GuestResponse response = GuestResponse.builder().id(1L).email("rahul.sharma@example.com").build();
        when(guestService.searchByEmail("rahul.sharma@example.com")).thenReturn(response);

        mockMvc.perform(get("/guests/search/email").param("email", "rahul.sharma@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("rahul.sharma@example.com"));
    }

    @Test
    void searchByName_shouldReturnList() throws Exception {
        GuestResponse response = GuestResponse.builder().id(1L).firstName("Rahul").build();
        when(guestService.searchByName("Rahul")).thenReturn(List.of(response));

        mockMvc.perform(get("/guests/search/name").param("name", "Rahul"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Rahul"));
    }
}
