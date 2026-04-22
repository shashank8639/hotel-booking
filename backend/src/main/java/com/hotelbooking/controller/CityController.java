package com.hotelbooking.controller;

import com.hotelbooking.dto.hotel.CityResponse;
import com.hotelbooking.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cities")
@RequiredArgsConstructor
public class CityController {

    private final HotelService hotelService;

    @GetMapping
    public ResponseEntity<List<CityResponse>> telanganaCities() {
        return ResponseEntity.ok(hotelService.listTelanganaCities());
    }

    @GetMapping("/popular")
    public ResponseEntity<List<CityResponse>> popular() {
        return ResponseEntity.ok(hotelService.popularCities());
    }
}
