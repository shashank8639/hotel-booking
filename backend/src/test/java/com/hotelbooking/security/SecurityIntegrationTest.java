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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full-stack security tests: filter chain + JWT + RBAC.
 * <p>
 * Uses real {@link SecurityFilterChain} (unlike standalone controller tests).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityIntegrationTest {

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
        // roles may be shared; ensure present
        IntegrationTestSupport.ensureRoles(roleRepository);

        var customer = IntegrationTestSupport.persistUser(
                userRepository, roleRepository, passwordEncoder,
                "customer@example.com", "password123", UserRole.CUSTOMER
        );
        var admin = IntegrationTestSupport.persistUser(
                userRepository, roleRepository, passwordEncoder,
                "admin@example.com", "password123", UserRole.ADMIN
        );
        customerBearer = IntegrationTestSupport.bearerAccessToken(jwtService, customer);
        adminBearer = IntegrationTestSupport.bearerAccessToken(jwtService, admin);
    }

    @Test
    void publicRooms_areAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/rooms").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void bookings_requireAuthentication() throws Exception {
        mockMvc.perform(get("/bookings").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void customerCanCheckAvailability() throws Exception {
        mockMvc.perform(get("/bookings/availability")
                        .header(SecurityConstants.AUTHORIZATION_HEADER, customerBearer)
                        .param("checkInDate", "2026-09-01")
                        .param("checkOutDate", "2026-09-03")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void customerCannotListAllBookings_adminOnly() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(SecurityConstants.AUTHORIZATION_HEADER, customerBearer)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanListAllBookings() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(SecurityConstants.AUTHORIZATION_HEADER, adminBearer)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void customerIsForbiddenFromAdminDashboard() throws Exception {
        mockMvc.perform(get("/admin/dashboard")
                        .header(SecurityConstants.AUTHORIZATION_HEADER, customerBearer)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanAccessDashboard() throws Exception {
        mockMvc.perform(get("/admin/dashboard")
                        .header(SecurityConstants.AUTHORIZATION_HEADER, adminBearer)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void blankBearerTokenIsTreatedAsUnauthenticated() throws Exception {
        // Empty token after "Bearer " is ignored by the filter (no JWT parse).
        mockMvc.perform(get("/bookings")
                        .header(SecurityConstants.AUTHORIZATION_HEADER, "Bearer ")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
