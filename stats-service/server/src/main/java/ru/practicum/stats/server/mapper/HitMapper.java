package ru.practicum.stats.server.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.server.model.Hit;

@Component
public class HitMapper {
    public Hit toEntity(EndpointHitDto dto) {
        return Hit.builder()
                .app(dto.app())
                .uri(dto.uri())
                .ip(dto.ip())
                .timestamp(dto.timestamp())
                .build();
    }
}
