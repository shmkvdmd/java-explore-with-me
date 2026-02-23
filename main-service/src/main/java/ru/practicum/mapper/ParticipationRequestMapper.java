package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.model.ParticipationRequest;

@Component
public class ParticipationRequestMapper {
    public ParticipationRequestDto toDto(ParticipationRequest request) {
        if (request == null) {
            return null;
        }
        return ParticipationRequestDto
                .builder()
                .id(request.getId())
                .created(request.getCreated())
                .event(request.getEvent() == null ? null : request.getEvent().getId())
                .requester(request.getRequester() == null ? null : request.getRequester().getId())
                .status(request.getStatus())
                .build();
    }
}
