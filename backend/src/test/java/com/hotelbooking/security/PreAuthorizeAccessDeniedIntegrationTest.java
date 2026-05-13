package com.hotelbooking.security;

import com.hotelbooking.repository.RoleRepository;
import com.hotelbooking.repository.UserRepository;
import com.hotelbooking.util.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies {@code @PreAuthorize} + JSON 403 from {@link JwtAccessDeniedHandler}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PreAuthorizeAccessDeniedIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String customerBearer;
    private String adminBearer;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        IntegrationTestSupport.ensureRoles(roleRepository);

        var customer = IntegrationTestSupport.persistUser(
                userRepository, roleRepository, passwordEncoder,
                "preauth.customer@example.com", "password123", UserRole.CUSTOMER
        );
        var admin = IntegrationTestSupport.persistUser(
                userRepository, roleRepository, passwordEncoder,
                "preauth.admin@example.com", "password123", UserRole.ADMIN
        );
        customerBearer = IntegrationTestSupport.bearerAccessToken(jwtService, customer);
        adminBearer = IntegrationTestSupport.bearerAccessToken(jwtService, admin);
    }

    @Test
    void adminPing_forbiddenForCustomer_returns403Json() throws Exception {
        mockMvc.perform(get("/practice/security/admin-ping")
                        .header(SecurityConstants.AUTHORIZATION_HEADER, customerBearer)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    void adminPing_okForAdmin() throws Exception {
        mockMvc.perform(get("/practice/security/admin-ping")
                        .header(SecurityConstants.AUTHORIZATION_HEADER, adminBearer)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("admin-ok"));
    }

    @Test
    void authPing_okForCustomer() throws Exception {
        mockMvc.perform(get("/practice/security/auth-ping")
                        .header(SecurityConstants.AUTHORIZATION_HEADER, customerBearer)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("auth-ok"));
    }
}
