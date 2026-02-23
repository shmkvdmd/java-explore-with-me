package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record NewEventDto(
        @NotBlank
        @Size(min = 20, max = 2000)
        String annotation,

        @NotNull
        Long category,

        @NotBlank
        @Size(min = 20, max = 7000)
        String description,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Future
        @NotNull
        LocalDateTime eventDate,

        @Valid
        @NotNull
        LocationDto location,

        Boolean paid,

        @PositiveOrZero
        Integer participantLimit,

        Boolean requestModeration,

        @NotBlank
        @Size(min = 3, max = 120)
        String title
) {
}
