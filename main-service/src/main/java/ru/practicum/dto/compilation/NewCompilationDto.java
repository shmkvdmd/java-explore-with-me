package ru.practicum.dto.compilation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.Set;

@Builder
public record NewCompilationDto(
        Set<Long> events,

        Boolean pinned,

        @NotBlank
        @Size(min = 1, max = 50)
        String title
) {
}
