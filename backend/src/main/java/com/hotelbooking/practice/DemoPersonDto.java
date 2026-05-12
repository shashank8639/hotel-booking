package com.hotelbooking.practice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Plain practice target type for MapStruct (API-shaped DTO).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemoPersonDto {

    private Long id;
    private String fullName;
    private String email;
}
