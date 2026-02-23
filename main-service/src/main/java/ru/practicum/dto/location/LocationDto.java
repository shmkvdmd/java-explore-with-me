package ru.practicum.dto.location;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record LocationDto(
        @NotNull
        Float lat,

        @NotNull
        Float lon
) {
}
