package ru.practicum.stats.dto;

import lombok.Builder;

@Builder
public record ViewStatsDto(
        String app,
        String uri,
        Long hits
) {}