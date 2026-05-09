package com.hotelbooking.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelbooking.dto.auth.LoginRequest;
import com.hotelbooking.dto.auth.RegisterRequest;
import com.hotelbooking.repository.RoleRepository;
import com.hotelbooking.repository.UserRepository;
import com.hotelbooking.security.UserRole;
import com.hotelbooking.util.IntegrationTestSupport;
import com.hotelbooking.util.MockMvcTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Authentication happy/negative path against the running Spring context + H2.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = MockMvcTestSupport.objectMapper();

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        IntegrationTestSupport.ensureRoles(roleRepository);
    }

    @Test
    void register_returnsTokens_andMeWorks() throws Exception {
        RegisterRequest register = RegisterRequest.builder()
                .firstName("Rahul")
                .lastName("Sharma")
                .email("rahul.flow@example.com")
                .password("password123")
                .build();

        MvcResult registerResult = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();

        JsonNode body = objectMapper.readTree(registerResult.getResponse().getContentAsString());
        String accessToken = body.get("accessToken").asText();

        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("rahul.flow@example.com"));

        assertThat(userRepository.existsByEmail("rahul.flow@example.com")).isTrue();
    }

    @Test
    void login_withPersistedUser_returnsAccessToken() throws Exception {
        // Persist without minting a refresh token first — avoids same-second JWT uniqueness clashes
        // that occur when register + login both insert identical refresh JWTs in one second.
        IntegrationTestSupport.persistUser(
                userRepository, roleRepository, passwordEncoder,
                "login.flow@example.com", "password123", UserRole.CUSTOMER
        );

        LoginRequest login = LoginRequest.builder()
                .email("login.flow@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void login_withWrongPassword_returnsUnauthorized() throws Exception {
        IntegrationTestSupport.persistUser(
                userRepository, roleRepository, passwordEncoder,
                "priya.flow@example.com", "password123", UserRole.CUSTOMER
        );

        LoginRequest badLogin = LoginRequest.builder()
                .email("priya.flow@example.com")
                .password("wrong-password")
                .build();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badLogin)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_duplicateEmail_returnsConflict() throws Exception {
        RegisterRequest register = RegisterRequest.builder()
                .firstName("Dup")
                .lastName("User")
                .email("dup@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isConflict());
    }
}
