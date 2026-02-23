package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.model.Location;

@Component
public class LocationMapper {
    public LocationDto toDto(Location location) {
        if (location == null) {
            return null;
        }
        return LocationDto
                .builder()
                .lat(location.getLat())
                .lon(location.getLon())
                .build();
    }

    public Location fromDto(LocationDto dto) {
        if (dto == null) {
            return null;
        }
        return Location
                .builder()
                .lat(dto.lat())
                .lon(dto.lon())
                .build();
    }
}
