package ru.practicum.stats.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record EndpointHitDto(
        Long id,

        @NotBlank
        String app,

        @NotBlank
        String uri,

        @NotBlank
        String ip,

        @NotNull
        @PastOrPresent
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime timestamp
) {
}