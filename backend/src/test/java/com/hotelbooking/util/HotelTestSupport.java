package com.hotelbooking.util;

import com.hotelbooking.database.HotelCategory;
import com.hotelbooking.database.HotelStatus;
import com.hotelbooking.entity.City;
import com.hotelbooking.entity.Country;
import com.hotelbooking.entity.Hotel;
import com.hotelbooking.entity.State;
import com.hotelbooking.repository.CityRepository;
import com.hotelbooking.repository.CountryRepository;
import com.hotelbooking.repository.HotelRepository;
import com.hotelbooking.repository.StateRepository;

/**
 * Persists a minimal geo + hotel graph for tests after Module 16 ({@code rooms.hotel_id} required).
 */
public final class HotelTestSupport {

    public static final String DEFAULT_HOTEL_SLUG = "grand-horizon-hyderabad";

    private HotelTestSupport() {
    }

    public static Hotel persistSampleHotel(
            CountryRepository countryRepository,
            StateRepository stateRepository,
            CityRepository cityRepository,
            HotelRepository hotelRepository
    ) {
        return hotelRepository.findBySlug(DEFAULT_HOTEL_SLUG).orElseGet(() -> {
            Country country = countryRepository.findByCode("IN").orElseGet(() ->
                    countryRepository.save(Country.builder().code("IN").name("India").build()));
            State state = stateRepository.findByCountry_CodeAndCode("IN", "TS").orElseGet(() ->
                    stateRepository.save(State.builder()
                            .country(country)
                            .code("TS")
                            .name("Telangana")
                            .build()));
            City city = cityRepository.findBySlug("hyderabad").orElseGet(() ->
                    cityRepository.save(City.builder()
                            .state(state)
                            .name("Hyderabad")
                            .slug("hyderabad")
                            .popular(true)
                            .build()));
            return hotelRepository.save(Hotel.builder()
                    .city(city)
                    .name("Grand Horizon Hyderabad")
                    .slug(DEFAULT_HOTEL_SLUG)
                    .category(HotelCategory.LUXURY)
                    .starRating(5)
                    .status(HotelStatus.APPROVED)
                    .verified(true)
                    .addressLine1("Banjara Hills")
                    .currency("INR")
                    .build());
        });
    }

    /** In-memory hotel for unit tests (no persistence). */
    public static Hotel sampleHotel(Long id) {
        Hotel hotel = Hotel.builder()
                .name("Grand Horizon Hyderabad")
                .slug(DEFAULT_HOTEL_SLUG)
                .category(HotelCategory.LUXURY)
                .starRating(5)
                .status(HotelStatus.APPROVED)
                .verified(true)
                .addressLine1("Banjara Hills")
                .currency("INR")
                .build();
        hotel.setId(id);
        return hotel;
    }
}
