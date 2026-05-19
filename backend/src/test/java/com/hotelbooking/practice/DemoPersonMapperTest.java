package com.hotelbooking.practice;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies MapStruct-generated mapping without loading Spring.
 */
class DemoPersonMapperTest {

    private final DemoPersonMapper mapper = Mappers.getMapper(DemoPersonMapper.class);

    @Test
    void toDto_combinesFirstAndLastName() {
        DemoPerson person = DemoPerson.builder()
                .id(1L)
                .firstName("Rahul")
                .lastName("Sharma")
                .email("rahul@example.com")
                .build();

        DemoPersonDto dto = mapper.toDto(person);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getFullName()).isEqualTo("Rahul Sharma");
        assertThat(dto.getEmail()).isEqualTo("rahul@example.com");
    }

    @Test
    void toEntity_splitsFullName() {
        DemoPersonDto dto = DemoPersonDto.builder()
                .id(2L)
                .fullName("Priya Patel")
                .email("priya@example.com")
                .build();

        DemoPerson person = mapper.toEntity(dto);

        assertThat(person.getFirstName()).isEqualTo("Priya");
        assertThat(person.getLastName()).isEqualTo("Patel");
    }
}
