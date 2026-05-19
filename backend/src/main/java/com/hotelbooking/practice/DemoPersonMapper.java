package com.hotelbooking.practice;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Minimal MapStruct mapper — generates {@code DemoPersonMapperImpl} under
 * {@code target/generated-sources/annotations}.
 */
@Mapper(componentModel = "spring")
public interface DemoPersonMapper {

    @Mapping(target = "fullName", expression = "java(person.getFirstName() + \" \" + person.getLastName())")
    DemoPersonDto toDto(DemoPerson person);

    @Mapping(target = "firstName", source = "fullName", qualifiedByName = "firstNameFromFull")
    @Mapping(target = "lastName", source = "fullName", qualifiedByName = "lastNameFromFull")
    DemoPerson toEntity(DemoPersonDto dto);

    @Named("firstNameFromFull")
    default String extractFirst(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "";
        }
        return fullName.trim().split("\\s+", 2)[0];
    }

    @Named("lastNameFromFull")
    default String extractLast(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "";
        }
        String[] parts = fullName.trim().split("\\s+", 2);
        return parts.length > 1 ? parts[1] : "";
    }
}
