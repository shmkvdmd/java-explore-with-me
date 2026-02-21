package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import ru.practicum.enums.UserStateAction;

import java.time.LocalDateTime;

@Builder
public record UpdateEventUserRequest(
        @Size(min = 20, max = 2000)
        String annotation,

        Long category,

        @Size(min = 20, max = 7000)
        String description,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime eventDate,

        LocationDto location,

        Boolean paid,

        Integer participantLimit,

        Boolean requestModeration,

        UserStateAction stateAction,

        @Size(min = 3, max = 120)
        String title
) {
}
