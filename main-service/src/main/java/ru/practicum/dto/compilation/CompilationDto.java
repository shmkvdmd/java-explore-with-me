package ru.practicum.dto.compilation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import ru.practicum.dto.event.EventShortDto;

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
