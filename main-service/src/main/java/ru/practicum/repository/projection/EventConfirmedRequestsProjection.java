package ru.practicum.repository.projection;

public interface EventConfirmedRequestsProjection {
    Long getEventId();

    Long getConfirmedRequests();
}
