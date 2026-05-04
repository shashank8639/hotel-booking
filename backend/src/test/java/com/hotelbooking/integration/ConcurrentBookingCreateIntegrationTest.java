package com.hotelbooking.integration;

import com.hotelbooking.database.RoomType;
import com.hotelbooking.dto.BookingRequest;
import com.hotelbooking.entity.Guest;
import com.hotelbooking.entity.Room;
import com.hotelbooking.exception.RoomAlreadyBookedException;
import com.hotelbooking.repository.GuestRepository;
import com.hotelbooking.repository.RoomRepository;
import com.hotelbooking.service.BookingService;
import com.hotelbooking.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies pessimistic locking under concurrent create for the same room/dates.
 * Intentionally not {@code @Transactional} so each service call commits independently.
 */
@SpringBootTest
@ActiveProfiles("test")
class ConcurrentBookingCreateIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private RoomRepository roomRepository;

    private Guest guest;
    private Room room;
    private LocalDate checkIn;
    private LocalDate checkOut;

    @BeforeEach
    void setUp() {
        guest = guestRepository.save(Guest.builder()
                .firstName("Race")
                .lastName("Guest")
                .email("race-" + System.nanoTime() + "@example.com")
                .phone("+91-9000000099")
                .build());

        room = roomRepository.save(TestDataFactory.availableRoom(
                "R" + (System.nanoTime() % 100_000),
                RoomType.DELUXE,
                new BigDecimal("5000.00")
        ));

        checkIn = LocalDate.now().plusDays(40);
        checkOut = checkIn.plusDays(2);
    }

    @Test
    void concurrentCreate_sameRoom_onlyOneSucceeds() throws Exception {
        BookingRequest request = BookingRequest.builder()
                .guestId(guest.getId())
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .roomIds(List.of(room.getId()))
                .build();

        int threads = 2;
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger conflicts = new AtomicInteger();
        AtomicInteger otherFailures = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            futures.add(pool.submit(() -> {
                try {
                    start.await(5, TimeUnit.SECONDS);
                    bookingService.createBooking(request);
                    successes.incrementAndGet();
                } catch (RoomAlreadyBookedException ex) {
                    conflicts.incrementAndGet();
                } catch (Exception ex) {
                    otherFailures.incrementAndGet();
                }
            }));
        }

        start.countDown();
        for (Future<?> future : futures) {
            future.get(15, TimeUnit.SECONDS);
        }
        pool.shutdownNow();

        assertThat(otherFailures.get()).as("unexpected failures").isZero();
        assertThat(successes.get()).isEqualTo(1);
        assertThat(conflicts.get()).isEqualTo(1);
    }
}
