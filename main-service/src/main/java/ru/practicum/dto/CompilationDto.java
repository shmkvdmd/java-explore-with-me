package ru.practicum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.Set;

@Builder
public record CompilationDto(
        Set<EventShortDto> events,

        @NotNull
        Long id,

        @NotNull
        Boolean pinned,

        @NotBlank
        String title
) {
}
