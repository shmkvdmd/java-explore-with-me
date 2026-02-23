package ru.practicum.service;

import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.NewEventDto;
import ru.practicum.dto.UpdateEventAdminRequest;
import ru.practicum.dto.UpdateEventUserRequest;
import ru.practicum.enums.EventSort;
import ru.practicum.enums.EventState;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    List<EventShortDto> getUserEvents(Long userId, int from, int size);

    EventFullDto addEvent(Long userId, NewEventDto dto);

    EventFullDto getUserEvent(Long userId, Long eventId);

    EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest dto);

    List<EventFullDto> searchAdminEvents(List<Long> users,
                                         List<EventState> states,
                                         List<Long> categories,
                                         LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd,
                                         int from,
                                         int size);

    EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequest dto);

    List<EventShortDto> searchPublicEvents(String text,
                                           List<Long> categories,
                                           Boolean paid,
                                           LocalDateTime rangeStart,
                                           LocalDateTime rangeEnd,
                                           Boolean onlyAvailable,
                                           EventSort sort,
                                           int from,
                                           int size,
                                           String ip,
                                           String uri);

    EventFullDto getPublicEvent(Long eventId, String ip, String uri);
}
