package ru.practicum.dto;

import lombok.Builder;
import ru.practicum.enums.ParticipationRequestStatus;

import java.time.LocalDateTime;

@Builder
public record ParticipationRequestDto(
        LocalDateTime created,
        Long event,
        Long id,
        Long requester,
        ParticipationRequestStatus status
) {
}
