package ru.practicum.stats.client;

import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class StatsClient {
    private final RestTemplate restTemplate;
    private final String serverUrl;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClient(@Value("${stats-server.url}") String serverUrl) {
        this.serverUrl = serverUrl;
        this.restTemplate = new RestTemplateBuilder()
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build();
    }

    public void saveHit(EndpointHitDto hitDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EndpointHitDto> request = new HttpEntity<>(hitDto, headers);
        URI uri = UriComponentsBuilder.fromUriString(serverUrl)
                .path("/hit")
                .build()
                .toUri();
        restTemplate.exchange(uri, HttpMethod.POST, request, Void.class);
    }

    public List<ViewStatsDto> getStats(LocalDateTime start,
                                       LocalDateTime end,
                                       @Nullable List<String> uris,
                                       boolean unique) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(serverUrl)
                .path("/stats")
                .queryParam("start", start.format(FORMATTER))
                .queryParam("end", end.format(FORMATTER))
                .queryParam("unique", unique);

        if (uris != null && !uris.isEmpty()) {
            builder.queryParam("uris", uris.toArray());
        }

        URI uri = builder.build().encode().toUri();
        ViewStatsDto[] response = restTemplate.getForObject(uri, ViewStatsDto[].class);

        return response != null ? List.of(response) : List.of();
    }
}
