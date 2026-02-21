package ru.practicum.stats.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.server.mapper.HitMapper;
import ru.practicum.stats.server.model.Hit;
import ru.practicum.stats.server.repository.HitRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final HitRepository repository;
    private final HitMapper mapper;

    @Transactional
    public void saveHit(EndpointHitDto dto) {
        Hit hit = mapper.toEntity(dto);
        repository.save(hit);
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean isUnique) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start must be before end");
        }

        if (isUnique) {
            return repository.getUniqueStats(start, end, uris);
        } else {
            return repository.getStats(start, end, uris);
        }
    }
}
