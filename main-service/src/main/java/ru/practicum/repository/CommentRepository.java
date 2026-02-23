package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Comment;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByEventId(Long eventId, Pageable pageable);

    List<Comment> findAllByAuthorId(Long authorId, Pageable pageable);

    List<Comment> findAllByAuthorIdAndCreatedGreaterThanEqual(Long authorId, LocalDateTime createStart, Pageable pageable);

    List<Comment> findAllByAuthorIdAndCreatedLessThanEqual(Long authorId, LocalDateTime createEnd, Pageable pageable);

    List<Comment> findAllByAuthorIdAndCreatedBetween(Long authorId,
                                                     LocalDateTime createStart,
                                                     LocalDateTime createEnd,
                                                     Pageable pageable);
}
