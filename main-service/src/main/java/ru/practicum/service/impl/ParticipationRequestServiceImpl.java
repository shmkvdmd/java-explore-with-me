package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.enums.ParticipationRequestStatus;
import ru.practicum.enums.RequestStatusUpdateAction;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.ParticipationRequestMapper;
import ru.practicum.model.Event;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.model.User;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.ParticipationRequestRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.ParticipationRequestService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static ru.practicum.enums.EventState.PUBLISHED;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationRequestServiceImpl implements ParticipationRequestService {
    private final ParticipationRequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ParticipationRequestMapper requestMapper;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        getUser(userId);
        return requestRepository.findByRequesterId(userId)
                .stream()
                .map(requestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        User requester = getUser(userId);
        Event event = getEvent(eventId);

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Initiator cannot add request to own event");
        }
        if (event.getState() != PUBLISHED) {
            throw new ConflictException("Cannot participate in unpublished event");
        }
        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Request already exists");
        }

        long confirmed = requestRepository.countByEventIdAndStatus(eventId, ParticipationRequestStatus.CONFIRMED);
        if (event.getParticipantLimit() != 0 && confirmed >= event.getParticipantLimit()) {
            throw new ConflictException("The participant limit has been reached");
        }

        ParticipationRequestStatus status;
        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            status = ParticipationRequestStatus.CONFIRMED;
        } else {
            status = ParticipationRequestStatus.PENDING;
        }

        ParticipationRequest request = ParticipationRequest
                .builder()
                .created(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS))
                .event(event)
                .requester(requester)
                .status(status)
                .build();

        return requestMapper.toDto(requestRepository.save(request));
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        getUser(userId);
        ParticipationRequest request = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));
        request.setStatus(ParticipationRequestStatus.CANCELED);
        return requestMapper.toDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId) {
        getUser(userId);
        getOwnerEvent(userId, eventId);
        return requestRepository.findByEventId(eventId)
                .stream()
                .map(requestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId,
                                                              Long eventId,
                                                              EventRequestStatusUpdateRequest request) {
        getUser(userId);
        Event event = getOwnerEvent(userId, eventId);

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            throw new ConflictException("Request moderation is not required for this event");
        }
        if (request.requestIds() == null || request.requestIds().isEmpty()) {
            throw new BadRequestException("requestIds must not be empty");
        }

        List<ParticipationRequest> requests = requestRepository.findByEventIdAndIdIn(eventId, request.requestIds());
        if (requests.isEmpty()) {
            throw new NotFoundException("Requests were not found");
        }

        for (ParticipationRequest current : requests) {
            if (current.getStatus() != ParticipationRequestStatus.PENDING) {
                throw new ConflictException("Request must have status PENDING");
            }
        }

        List<ParticipationRequest> confirmed = new ArrayList<>();
        List<ParticipationRequest> rejected = new ArrayList<>();

        if (request.status() == RequestStatusUpdateAction.CONFIRMED) {
            long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, ParticipationRequestStatus.CONFIRMED);
            if (confirmedCount >= event.getParticipantLimit()) {
                throw new ConflictException("The participant limit has been reached");
            }

            for (ParticipationRequest current : requests) {
                if (confirmedCount < event.getParticipantLimit()) {
                    current.setStatus(ParticipationRequestStatus.CONFIRMED);
                    confirmed.add(current);
                    confirmedCount++;
                } else {
                    current.setStatus(ParticipationRequestStatus.REJECTED);
                    rejected.add(current);
                }
            }
            requestRepository.saveAll(requests);

            if (confirmedCount >= event.getParticipantLimit()) {
                Set<Long> processedIds = requests.stream().map(ParticipationRequest::getId).collect(HashSet::new, Set::add,
                        Set::addAll);
                List<ParticipationRequest> pending = requestRepository
                        .findByEventIdAndStatus(eventId, ParticipationRequestStatus.PENDING)
                        .stream()
                        .filter(current -> !processedIds.contains(current.getId()))
                        .toList();

                for (ParticipationRequest current : pending) {
                    current.setStatus(ParticipationRequestStatus.REJECTED);
                }
                requestRepository.saveAll(pending);
                rejected.addAll(pending);
            }
        } else {
            for (ParticipationRequest current : requests) {
                current.setStatus(ParticipationRequestStatus.REJECTED);
            }
            requestRepository.saveAll(requests);
            rejected.addAll(requests);
        }

        return EventRequestStatusUpdateResult
                .builder()
                .confirmedRequests(confirmed.stream().map(requestMapper::toDto).toList())
                .rejectedRequests(rejected.stream().map(requestMapper::toDto).toList())
                .build();
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }

    private Event getOwnerEvent(Long userId, Long eventId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }
}
