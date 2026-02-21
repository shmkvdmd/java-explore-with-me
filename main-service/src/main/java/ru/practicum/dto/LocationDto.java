package ru.practicum.dto;

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
