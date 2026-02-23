package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.enums.ParticipationRequestStatus;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.ParticipationRequestRepository;
import ru.practicum.repository.projection.EventConfirmedRequestsProjection;
import ru.practicum.service.CompilationService;
import ru.practicum.util.PageRequestUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final ParticipationRequestRepository requestRepository;
    private final CompilationMapper compilationMapper;
    private final EventStatsService eventStatsService;

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto dto) {
        Compilation compilation = Compilation
                .builder()
                .title(dto.title())
                .pinned(Boolean.TRUE.equals(dto.pinned()))
                .events(loadEvents(dto.events()))
                .build();

        Compilation saved = compilationRepository.save(compilation);
        enrichEvents(saved.getEvents());
        return compilationMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compilationId) {
        if (!compilationRepository.existsById(compilationId)) {
            throw new NotFoundException("Compilation with id=" + compilationId + " was not found");
        }
        compilationRepository.deleteById(compilationId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compilationId, UpdateCompilationRequest dto) {
        Compilation compilation = findCompilation(compilationId);
        if (dto.title() != null) {
            compilation.setTitle(dto.title());
        }
        if (dto.pinned() != null) {
            compilation.setPinned(dto.pinned());
        }
        if (dto.events() != null) {
            compilation.setEvents(loadEvents(dto.events()));
        }

        Compilation saved = compilationRepository.save(compilation);
        enrichEvents(saved.getEvents());
        return compilationMapper.toDto(saved);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        if (pinned == null) {
            return compilationRepository
                    .findAll(PageRequestUtil.toPageable(from, size))
                    .stream()
                    .peek(compilation -> enrichEvents(compilation.getEvents()))
                    .map(compilationMapper::toDto)
                    .toList();
        }
        return compilationRepository
                .findByPinned(pinned, PageRequestUtil.toPageable(from, size))
                .stream()
                .peek(compilation -> enrichEvents(compilation.getEvents()))
                .map(compilationMapper::toDto)
                .toList();
    }

    @Override
    public CompilationDto getCompilation(Long compilationId) {
        Compilation compilation = findCompilation(compilationId);
        enrichEvents(compilation.getEvents());
        return compilationMapper.toDto(compilation);
    }

    private Compilation findCompilation(Long compilationId) {
        return compilationRepository.findById(compilationId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compilationId + " was not found"));
    }

    private Set<Event> loadEvents(Set<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(eventRepository.findByIdIn(eventIds));
    }

    private void enrichEvents(Set<Event> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        List<Long> eventIds = events.stream().map(Event::getId).toList();

        Map<Long, Long> confirmedByEvent = requestRepository
                .countByEventIdsAndStatus(eventIds, ParticipationRequestStatus.CONFIRMED)
                .stream()
                .collect(Collectors.toMap(
                        EventConfirmedRequestsProjection::getEventId,
                        EventConfirmedRequestsProjection::getConfirmedRequests
                ));

        Map<Long, Long> viewsByEvent = eventStatsService.getViewsByEventIds(eventIds);

        for (Event event : events) {
            event.setConfirmedRequests(confirmedByEvent.getOrDefault(event.getId(), 0L));
            event.setViews(viewsByEvent.getOrDefault(event.getId(), 0L));
        }
    }
}
