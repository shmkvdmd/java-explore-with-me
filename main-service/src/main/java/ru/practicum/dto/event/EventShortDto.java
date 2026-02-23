package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.user.UserShortDto;

import java.time.LocalDateTime;

@Builder
public record EventShortDto(
        @NotBlank
        String annotation,

        @NotNull
        CategoryDto category,

        Long confirmedRequests,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @NotNull
        LocalDateTime eventDate,

        Long id,

        @NotNull
        UserShortDto initiator,

        @NotNull
        Boolean paid,

        @NotBlank
        String title,

        Long views
) {
}
