package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import ru.practicum.enums.AdminStateAction;

import java.time.LocalDateTime;

@Builder
public record UpdateEventAdminRequest(
        String annotation,

        Long category,

        String description,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime eventDate,

        LocationDto location,

        Boolean paid,

        Integer participantLimit,

        Boolean requestModeration,

        AdminStateAction stateAction,

        String title
) {
}
