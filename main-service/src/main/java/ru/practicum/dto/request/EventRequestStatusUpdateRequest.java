package ru.practicum.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import ru.practicum.enums.RequestStatusUpdateAction;

import java.util.List;

@Builder
public record EventRequestStatusUpdateRequest(
        @NotEmpty
        List<Long> requestIds,

        @NotNull
        RequestStatusUpdateAction status
) {
}
