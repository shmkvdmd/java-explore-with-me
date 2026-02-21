package ru.practicum.dto;

import lombok.Builder;
import ru.practicum.enums.RequestStatusUpdateAction;

import java.util.List;

@Builder
public record EventRequestStatusUpdateRequest(
        List<Long> requestIds,
        RequestStatusUpdateAction status
) {
}
