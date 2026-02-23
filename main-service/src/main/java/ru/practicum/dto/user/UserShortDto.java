package ru.practicum.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UserShortDto(
        @NotNull
        Long id,

        @NotBlank
        String name
) {
}
