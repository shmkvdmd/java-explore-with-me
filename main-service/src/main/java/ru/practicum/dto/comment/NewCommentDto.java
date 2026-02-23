package ru.practicum.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record NewCommentDto(
        @NotBlank
        @Size(min = 20, max = 250)
        String text
) {
}
