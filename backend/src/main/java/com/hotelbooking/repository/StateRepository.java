package com.hotelbooking.repository;

import com.hotelbooking.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StateRepository extends JpaRepository<State, Long> {
    List<State> findByCountry_CodeOrderByNameAsc(String countryCode);

    Optional<State> findByCountry_CodeAndCode(String countryCode, String stateCode);
}
