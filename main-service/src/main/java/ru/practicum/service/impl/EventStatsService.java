package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EventStatsService {
    private static final String APP_NAME = "ewm-main-service";
    private static final LocalDateTime START_DATE = LocalDateTime.of(1970, 1, 1, 0, 0);

    private final StatsClient statsClient;

    public void saveHit(String uri, String ip) {
        EndpointHitDto hit = EndpointHitDto
                .builder()
                .app(APP_NAME)
                .uri(uri)
                .ip(ip)
                .timestamp(LocalDateTime.now())
                .build();
        statsClient.saveHit(hit);
    }

    public Map<Long, Long> getViewsByEventIds(Collection<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }

        List<String> uris = eventIds.stream()
                .map(id -> "/events/" + id)
                .toList();

        List<ViewStatsDto> stats = statsClient.getStats(START_DATE, LocalDateTime.now(), uris, true);
        Map<Long, Long> views = new HashMap<>();
        for (ViewStatsDto stat : stats) {
            Long eventId = parseEventId(stat.uri());
            if (eventId != null) {
                views.put(eventId, stat.hits() == null ? 0L : stat.hits());
            }
        }
        return views;
    }

    private Long parseEventId(String uri) {
        if (uri == null || !uri.startsWith("/events/")) {
            return null;
        }
        try {
            return Long.parseLong(uri.substring("/events/".length()));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
