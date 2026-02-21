package ru.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.enums.EventState;
import ru.practicum.model.Event;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findByInitiatorId(Long initiatorId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long initiatorId);

    List<Event> findByIdIn(Collection<Long> ids);

    boolean existsByCategoryId(Long categoryId);

    @Query("""
            select e from Event e
            where (:usersEmpty = true or e.initiator.id in :users)
              and (:statesEmpty = true or e.state in :states)
              and (:categoriesEmpty = true or e.category.id in :categories)
              and (:rangeStartEmpty = true or e.eventDate >= :rangeStart)
              and (:rangeEndEmpty = true or e.eventDate <= :rangeEnd)
            """)
    Page<Event> searchAdminEvents(@Param("users") List<Long> users,
                                  @Param("usersEmpty") boolean usersEmpty,
                                  @Param("states") List<EventState> states,
                                  @Param("statesEmpty") boolean statesEmpty,
                                  @Param("categories") List<Long> categories,
                                  @Param("categoriesEmpty") boolean categoriesEmpty,
                                  @Param("rangeStartEmpty") boolean rangeStartEmpty,
                                  @Param("rangeStart") LocalDateTime rangeStart,
                                  @Param("rangeEndEmpty") boolean rangeEndEmpty,
                                  @Param("rangeEnd") LocalDateTime rangeEnd,
                                  Pageable pageable);

    @Query("""
            select e from Event e
            where e.state = ru.practicum.enums.EventState.PUBLISHED
              and (:textEmpty = true
                   or lower(e.annotation) like :textPattern
                   or lower(e.description) like :textPattern)
              and (:categoriesEmpty = true or e.category.id in :categories)
              and (:paid is null or e.paid = :paid)
              and (:rangeStartEmpty = true or e.eventDate >= :rangeStart)
              and (:rangeEndEmpty = true or e.eventDate <= :rangeEnd)
            """)
    Page<Event> searchPublicEvents(@Param("textEmpty") boolean textEmpty,
                                   @Param("textPattern") String textPattern,
                                   @Param("categories") List<Long> categories,
                                   @Param("categoriesEmpty") boolean categoriesEmpty,
                                   @Param("paid") Boolean paid,
                                   @Param("rangeStartEmpty") boolean rangeStartEmpty,
                                   @Param("rangeStart") LocalDateTime rangeStart,
                                   @Param("rangeEndEmpty") boolean rangeEndEmpty,
                                   @Param("rangeEnd") LocalDateTime rangeEnd,
                                   Pageable pageable);

    @Query("""
            select e from Event e
            where e.state = ru.practicum.enums.EventState.PUBLISHED
              and (:textEmpty = true
                   or lower(e.annotation) like :textPattern
                   or lower(e.description) like :textPattern)
              and (:categoriesEmpty = true or e.category.id in :categories)
              and (:paid is null or e.paid = :paid)
              and (:rangeStartEmpty = true or e.eventDate >= :rangeStart)
              and (:rangeEndEmpty = true or e.eventDate <= :rangeEnd)
            """)
    List<Event> searchPublicEventsRaw(@Param("textEmpty") boolean textEmpty,
                                      @Param("textPattern") String textPattern,
                                      @Param("categories") List<Long> categories,
                                      @Param("categoriesEmpty") boolean categoriesEmpty,
                                      @Param("paid") Boolean paid,
                                      @Param("rangeStartEmpty") boolean rangeStartEmpty,
                                      @Param("rangeStart") LocalDateTime rangeStart,
                                      @Param("rangeEndEmpty") boolean rangeEndEmpty,
                                      @Param("rangeEnd") LocalDateTime rangeEnd);
}
