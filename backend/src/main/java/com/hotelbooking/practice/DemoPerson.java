package com.hotelbooking.practice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Plain practice source type for MapStruct (not a JPA entity).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemoPerson {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
}
