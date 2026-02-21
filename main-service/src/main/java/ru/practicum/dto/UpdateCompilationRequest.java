package ru.practicum.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.Set;

@Builder
public record UpdateCompilationRequest(
        Set<Long> events,

        Boolean pinned,

        @Size(min = 1, max = 50)
        String title
) {
}
