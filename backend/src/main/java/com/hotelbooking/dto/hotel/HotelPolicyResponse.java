package com.hotelbooking.dto.hotel;

import com.hotelbooking.database.HotelPolicyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelPolicyResponse {
    private Long id;
    private HotelPolicyType policyType;
    private String title;
    private String body;
}
