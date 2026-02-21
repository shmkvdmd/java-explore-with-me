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
              and (:rangeStart is null or e.eventDate >= :rangeStart)
              and (:rangeEnd is null or e.eventDate <= :rangeEnd)
            """)
    Page<Event> searchAdminEvents(@Param("users") List<Long> users,
                                  @Param("usersEmpty") boolean usersEmpty,
                                  @Param("states") List<EventState> states,
                                  @Param("statesEmpty") boolean statesEmpty,
                                  @Param("categories") List<Long> categories,
                                  @Param("categoriesEmpty") boolean categoriesEmpty,
                                  @Param("rangeStart") LocalDateTime rangeStart,
                                  @Param("rangeEnd") LocalDateTime rangeEnd,
                                  Pageable pageable);

    @Query("""
            select e from Event e
            where e.state = ru.practicum.enums.EventState.PUBLISHED
              and (:text is null
                   or lower(e.annotation) like lower(concat('%', :text, '%'))
                   or lower(e.description) like lower(concat('%', :text, '%')))
              and (:categoriesEmpty = true or e.category.id in :categories)
              and (:paid is null or e.paid = :paid)
              and (:rangeStart is null or e.eventDate >= :rangeStart)
              and (:rangeEnd is null or e.eventDate <= :rangeEnd)
            """)
    Page<Event> searchPublicEvents(@Param("text") String text,
                                   @Param("categories") List<Long> categories,
                                   @Param("categoriesEmpty") boolean categoriesEmpty,
                                   @Param("paid") Boolean paid,
                                   @Param("rangeStart") LocalDateTime rangeStart,
                                   @Param("rangeEnd") LocalDateTime rangeEnd,
                                   Pageable pageable);

    @Query("""
            select e from Event e
            where e.state = ru.practicum.enums.EventState.PUBLISHED
              and (:text is null
                   or lower(e.annotation) like lower(concat('%', :text, '%'))
                   or lower(e.description) like lower(concat('%', :text, '%')))
              and (:categoriesEmpty = true or e.category.id in :categories)
              and (:paid is null or e.paid = :paid)
              and (:rangeStart is null or e.eventDate >= :rangeStart)
              and (:rangeEnd is null or e.eventDate <= :rangeEnd)
            """)
    List<Event> searchPublicEventsRaw(@Param("text") String text,
                                      @Param("categories") List<Long> categories,
                                      @Param("categoriesEmpty") boolean categoriesEmpty,
                                      @Param("paid") Boolean paid,
                                      @Param("rangeStart") LocalDateTime rangeStart,
                                      @Param("rangeEnd") LocalDateTime rangeEnd);
}
