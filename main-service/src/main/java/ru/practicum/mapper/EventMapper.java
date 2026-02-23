package ru.practicum.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.NewEventDto;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.User;

@Component
@RequiredArgsConstructor
public class EventMapper {
    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;
    private final LocationMapper locationMapper;

    public EventShortDto toShortDto(Event event) {
        if (event == null) {
            return null;
        }
        return EventShortDto
                .builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(categoryMapper.toDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests())
                .eventDate(event.getEventDate())
                .initiator(userMapper.toShortDto(event.getInitiator()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(event.getViews())
                .build();
    }

    public EventFullDto toFullDto(Event event) {
        if (event == null) {
            return null;
        }
        return EventFullDto
                .builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(categoryMapper.toDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests())
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(userMapper.toShortDto(event.getInitiator()))
                .location(locationMapper.toDto(event.getLocation()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .views(event.getViews())
                .build();
    }

    public Event fromNewDto(NewEventDto dto, Category category, User initiator) {
        if (dto == null) {
            return null;
        }
        return Event
                .builder()
                .annotation(dto.annotation())
                .category(category)
                .description(dto.description())
                .eventDate(dto.eventDate())
                .initiator(initiator)
                .location(locationMapper.fromDto(dto.location()))
                .paid(Boolean.TRUE.equals(dto.paid()))
                .participantLimit(dto.participantLimit() == null ? 0 : dto.participantLimit())
                .requestModeration(dto.requestModeration() == null || dto.requestModeration())
                .title(dto.title())
                .build();
    }
}
