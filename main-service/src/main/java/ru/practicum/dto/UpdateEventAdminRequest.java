package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import ru.practicum.enums.AdminStateAction;

import java.time.LocalDateTime;

@Builder
public record UpdateEventAdminRequest(
        @Size(min = 20, max = 2000)
        String annotation,

        Long category,

        @Size(min = 20, max = 7000)
        String description,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Future
        LocalDateTime eventDate,

        @Valid
        LocationDto location,

        Boolean paid,

        @PositiveOrZero
        Integer participantLimit,

        Boolean requestModeration,

        AdminStateAction stateAction,

        @Size(min = 3, max = 120)
        String title
) {
}
