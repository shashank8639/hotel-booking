package com.hotelbooking.repository;

import com.hotelbooking.entity.Guest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class GuestRepositoryTest {

    @Autowired
    private GuestRepository guestRepository;

    @BeforeEach
    void setUp() {
        guestRepository.save(Guest.builder()
                .firstName("Rahul")
                .lastName("Sharma")
                .email("rahul.sharma@example.com")
                .phone("+91-9876543210")
                .build());

        guestRepository.save(Guest.builder()
                .firstName("Priya")
                .lastName("Patel")
                .email("priya.patel@example.com")
                .phone("+91-9876543211")
                .build());
    }

    @Test
    void findByEmail_shouldReturnGuest() {
        var guest = guestRepository.findByEmail("rahul.sharma@example.com");

        assertThat(guest).isPresent();
        assertThat(guest.get().getFirstName()).isEqualTo("Rahul");
    }

    @Test
    void findByPhone_shouldReturnGuest() {
        var guest = guestRepository.findByPhone("+91-9876543210");

        assertThat(guest).isPresent();
        assertThat(guest.get().getLastName()).isEqualTo("Sharma");
    }

    @Test
    void searchByName_shouldSupportPartialMatch() {
        List<Guest> results = guestRepository.searchByName("pat");

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getFirstName()).isEqualTo("Priya");
    }

    @Test
    void existsByEmail_shouldReturnTrueForExistingEmail() {
        assertThat(guestRepository.existsByEmail("rahul.sharma@example.com")).isTrue();
        assertThat(guestRepository.existsByEmail("missing@example.com")).isFalse();
    }
}
