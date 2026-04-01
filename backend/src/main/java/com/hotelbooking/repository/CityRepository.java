package com.hotelbooking.repository;

import com.hotelbooking.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CityRepository extends JpaRepository<City, Long> {
    List<City> findByState_CodeOrderByNameAsc(String stateCode);

    List<City> findByPopularTrueOrderByNameAsc();

    Optional<City> findBySlug(String slug);

    List<City> findByNameContainingIgnoreCaseOrderByNameAsc(String name);
}
