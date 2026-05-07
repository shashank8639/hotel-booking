package com.hotelbooking.integration;

import com.hotelbooking.repository.RoleRepository;
import com.hotelbooking.repository.UserRepository;
import com.hotelbooking.security.JwtService;
import com.hotelbooking.security.SecurityConstants;
import com.hotelbooking.security.UserRole;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReportSecurityIntegrationTest {

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

    private String adminBearer;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        IntegrationTestSupport.ensureRoles(roleRepository);
        var admin = IntegrationTestSupport.persistUser(
                userRepository, roleRepository, passwordEncoder,
                "reports.admin@example.com", "password123", UserRole.ADMIN
        );
        adminBearer = IntegrationTestSupport.bearerAccessToken(jwtService, admin);
    }

    @Test
    void dashboardAndRevenue_requireAdmin() throws Exception {
        mockMvc.perform(get("/admin/dashboard").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/admin/dashboard")
                        .header(SecurityConstants.AUTHORIZATION_HEADER, adminBearer)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRooms").exists());

        mockMvc.perform(get("/admin/reports/revenue")
                        .header(SecurityConstants.AUTHORIZATION_HEADER, adminBearer)
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-01-31")
                        .param("period", "MONTHLY")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
