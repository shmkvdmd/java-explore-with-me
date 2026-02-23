package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import ru.practicum.enums.EventState;

import java.time.LocalDateTime;

@Builder
public record EventFullDto(
        @NotBlank
        String annotation,

        @NotNull
        CategoryDto category,

        Long confirmedRequests,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdOn,

        String description,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @NotNull
        LocalDateTime eventDate,

        Long id,

        @NotNull
        UserShortDto initiator,

        @NotNull
        LocationDto location,

        @NotNull
        Boolean paid,

        Integer participantLimit,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime publishedOn,

        Boolean requestModeration,

        EventState state,

        @NotBlank
        String title,

        Long views
) {
}
