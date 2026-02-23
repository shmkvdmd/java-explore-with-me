package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.LocationDto;
import ru.practicum.dto.NewEventDto;
import ru.practicum.dto.UpdateEventAdminRequest;
import ru.practicum.dto.UpdateEventUserRequest;
import ru.practicum.enums.AdminStateAction;
import ru.practicum.enums.EventSort;
import ru.practicum.enums.EventState;
import ru.practicum.enums.ParticipationRequestStatus;
import ru.practicum.enums.UserStateAction;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.LocationMapper;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.User;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.ParticipationRequestRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.repository.projection.EventConfirmedRequestsProjection;
import ru.practicum.service.EventService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ParticipationRequestRepository requestRepository;
    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;
    private final EventStatsService eventStatsService;

    @Override
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        getUser(userId);
        List<Event> events = eventRepository.findByInitiatorId(userId, toPageable(from, size, null)).toList();
        enrichEvents(events);
        return events.stream().map(eventMapper::toShortDto).toList();
    }

    @Override
    @Transactional
    public EventFullDto addEvent(Long userId, NewEventDto dto) {
        User user = getUser(userId);
        Category category = getCategory(dto.category());
        validateEventDateAfterNow(dto.eventDate(), 2,
                "Field: eventDate. Error: must contain a date at least 2 hours in the future.");

        Event event = eventMapper.fromNewDto(dto, category, user);
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());

        Event saved = eventRepository.save(event);
        enrichEvents(List.of(saved));
        return eventMapper.toFullDto(saved);
    }

    @Override
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        getUser(userId);
        Event event = getUserEventEntity(userId, eventId);
        enrichEvents(List.of(event));
        return eventMapper.toFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest dto) {
        getUser(userId);
        Event event = getUserEventEntity(userId, eventId);

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }

        if (dto.eventDate() != null) {
            validateEventDateAfterNow(dto.eventDate(), 2,
                    "Field: eventDate. Error: must contain a date at least 2 hours in the future.");
        }

        applyEventPatch(event, dto.annotation(), dto.description(), dto.category(), dto.eventDate(), dto.location(),
                dto.paid(), dto.participantLimit(), dto.requestModeration(), dto.title());

        if (dto.stateAction() != null) {
            if (dto.stateAction() == UserStateAction.SEND_TO_REVIEW) {
                event.setState(EventState.PENDING);
            } else if (dto.stateAction() == UserStateAction.CANCEL_REVIEW) {
                event.setState(EventState.CANCELED);
            }
        }

        Event saved = eventRepository.save(event);
        enrichEvents(List.of(saved));
        return eventMapper.toFullDto(saved);
    }

    @Override
    public List<EventFullDto> searchAdminEvents(List<Long> users,
                                                List<EventState> states,
                                                List<Long> categories,
                                                LocalDateTime rangeStart,
                                                LocalDateTime rangeEnd,
                                                int from,
                                                int size) {
        validateRange(rangeStart, rangeEnd);
        List<Long> userFilter = normalizeLongFilter(users, -1L);
        List<EventState> stateFilter = normalizeStateFilter(states, EventState.PENDING);
        List<Long> categoryFilter = normalizeLongFilter(categories, -1L);
        boolean rangeStartEmpty = rangeStart == null;
        boolean rangeEndEmpty = rangeEnd == null;
        LocalDateTime safeRangeStart = rangeStartEmpty ? LocalDateTime.of(1970, 1, 1, 0, 0) : rangeStart;
        LocalDateTime safeRangeEnd = rangeEndEmpty ? LocalDateTime.of(3000, 1, 1, 0, 0) : rangeEnd;
        List<Event> events = eventRepository
                .searchAdminEvents(
                        userFilter,
                        users == null || users.isEmpty(),
                        stateFilter,
                        states == null || states.isEmpty(),
                        categoryFilter,
                        categories == null || categories.isEmpty(),
                        rangeStartEmpty,
                        safeRangeStart,
                        rangeEndEmpty,
                        safeRangeEnd,
                        toPageable(from, size, null))
                .toList();
        enrichEvents(events);
        return events.stream().map(eventMapper::toFullDto).toList();
    }

    @Override
    @Transactional
    public EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequest dto) {
        Event event = getEvent(eventId);

        applyEventPatch(event, dto.annotation(), dto.description(), dto.category(), dto.eventDate(), dto.location(),
                dto.paid(), dto.participantLimit(), dto.requestModeration(), dto.title());

        if (dto.stateAction() != null) {
            if (dto.stateAction() == AdminStateAction.PUBLISH_EVENT) {
                if (event.getState() != EventState.PENDING) {
                    throw new ConflictException("Cannot publish the event because it's not in the right state: "
                            + event.getState());
                }
                validateEventDateAfterNow(event.getEventDate(), 1,
                        "Event date must be at least 1 hour after publish time");
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            }
            if (dto.stateAction() == AdminStateAction.REJECT_EVENT) {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConflictException("Cannot reject the event because it is already published");
                }
                event.setState(EventState.CANCELED);
            }
        }

        Event saved = eventRepository.save(event);
        enrichEvents(List.of(saved));
        return eventMapper.toFullDto(saved);
    }

    @Override
    public List<EventShortDto> searchPublicEvents(String text,
                                                  List<Long> categories,
                                                  Boolean paid,
                                                  LocalDateTime rangeStart,
                                                  LocalDateTime rangeEnd,
                                                  Boolean onlyAvailable,
                                                  EventSort sort,
                                                  int from,
                                                  int size,
                                                  String ip,
                                                  String uri) {
        validateRange(rangeStart, rangeEnd);
        if (rangeStart == null && rangeEnd == null) {
            rangeStart = LocalDateTime.now();
        }

        eventStatsService.saveHit(uri, ip);
        List<Long> categoryFilter = normalizeLongFilter(categories, -1L);
        boolean textEmpty = text == null || text.isBlank();
        String textPattern = textEmpty ? "" : "%" + text.toLowerCase(Locale.ROOT) + "%";
        boolean rangeStartEmpty = rangeStart == null;
        boolean rangeEndEmpty = rangeEnd == null;
        LocalDateTime safeRangeStart = rangeStartEmpty ? LocalDateTime.of(1970, 1, 1, 0, 0) : rangeStart;
        LocalDateTime safeRangeEnd = rangeEndEmpty ? LocalDateTime.of(3000, 1, 1, 0, 0) : rangeEnd;

        List<Event> events;
        boolean requiresMemoryProcessing = Boolean.TRUE.equals(onlyAvailable) || sort == EventSort.VIEWS;
        if (requiresMemoryProcessing) {
            List<Event> allEvents = eventRepository.searchPublicEventsRaw(
                    textEmpty,
                    textPattern,
                    categoryFilter,
                    categories == null || categories.isEmpty(),
                    paid,
                    rangeStartEmpty,
                    safeRangeStart,
                    rangeEndEmpty,
                    safeRangeEnd
            );
            enrichEvents(allEvents);
            events = allEvents
                    .stream()
                    .filter(event -> !Boolean.TRUE.equals(onlyAvailable) || isAvailable(event))
                    .sorted(resolveComparator(sort))
                    .skip(from)
                    .limit(size)
                    .toList();
        } else {
            Pageable pageable = sort == EventSort.EVENT_DATE
                    ? toPageable(from, size, Sort.by(Sort.Direction.ASC, "eventDate"))
                    : toPageable(from, size, null);
            events = eventRepository.searchPublicEvents(
                            textEmpty,
                            textPattern,
                            categoryFilter,
                            categories == null || categories.isEmpty(),
                            paid,
                            rangeStartEmpty,
                            safeRangeStart,
                            rangeEndEmpty,
                            safeRangeEnd,
                            pageable)
                    .toList();
            enrichEvents(events);
        }
        return events.stream().map(eventMapper::toShortDto).toList();
    }

    @Override
    public EventFullDto getPublicEvent(Long eventId, String ip, String uri) {
        eventStatsService.saveHit(uri, ip);
        Event event = getEvent(eventId);
        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }
        enrichEvents(List.of(event));
        return eventMapper.toFullDto(event);
    }

    private void enrichEvents(Collection<Event> events) {
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

    private void applyEventPatch(Event event,
                                 String annotation,
                                 String description,
                                 Long categoryId,
                                 LocalDateTime eventDate,
                                 LocationDto location,
                                 Boolean paid,
                                 Integer participantLimit,
                                 Boolean requestModeration,
                                 String title) {
        if (annotation != null) {
            event.setAnnotation(annotation);
        }
        if (description != null) {
            event.setDescription(description);
        }
        if (categoryId != null) {
            event.setCategory(getCategory(categoryId));
        }
        if (eventDate != null) {
            event.setEventDate(eventDate);
        }
        if (location != null) {
            event.setLocation(locationMapper.fromDto(location));
        }
        if (paid != null) {
            event.setPaid(paid);
        }
        if (participantLimit != null) {
            event.setParticipantLimit(participantLimit);
        }
        if (requestModeration != null) {
            event.setRequestModeration(requestModeration);
        }
        if (title != null) {
            event.setTitle(title);
        }
    }

    private void validateRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new BadRequestException("rangeStart must be before rangeEnd");
        }
    }

    private void validateEventDateAfterNow(LocalDateTime eventDate, int minHours, String errorMessage) {
        if (eventDate == null || eventDate.isBefore(LocalDateTime.now().plusHours(minHours))) {
            throw new ConflictException(errorMessage);
        }
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }

    private Event getUserEventEntity(Long userId, Long eventId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
    }

    private Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + categoryId + " was not found"));
    }

    private Pageable toPageable(int from, int size, Sort sort) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be greater than 0");
        }
        if (sort == null) {
            return PageRequest.of(from / size, size);
        }
        return PageRequest.of(from / size, size, sort);
    }

    private Comparator<Event> resolveComparator(EventSort sort) {
        if (sort == EventSort.VIEWS) {
            return Comparator.comparing(Event::getViews, Comparator.nullsLast(Long::compareTo)).reversed();
        }
        if (sort == EventSort.EVENT_DATE) {
            return Comparator.comparing(Event::getEventDate);
        }
        return Comparator.comparing(Event::getId);
    }

    private boolean isAvailable(Event event) {
        if (event.getParticipantLimit() == null || event.getParticipantLimit() == 0) {
            return true;
        }
        long confirmed = event.getConfirmedRequests() == null ? 0L : event.getConfirmedRequests();
        return confirmed < event.getParticipantLimit();
    }

    private List<Long> normalizeLongFilter(List<Long> values, long emptyFallback) {
        if (values == null || values.isEmpty()) {
            return List.of(emptyFallback);
        }
        return values;
    }

    private List<EventState> normalizeStateFilter(List<EventState> values, EventState emptyFallback) {
        if (values == null || values.isEmpty()) {
            return List.of(emptyFallback);
        }
        return values;
    }
}
