package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.model.Comment;
import ru.practicum.model.Event;
import ru.practicum.model.User;
import ru.practicum.repository.CommentRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.CommentService;
import ru.practicum.util.PageRequestUtil;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto dto) {
        User user = getUser(userId);
        Event event = getEvent(eventId);

        Comment comment = Comment.builder()
                .text(dto.text())
                .created(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS))
                .author(user)
                .event(event)
                .build();

        return commentMapper.toDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public CommentDto updateCommentByUser(Long userId, Long commentId, NewCommentDto dto) {
        getUser(userId);
        Comment comment = getComment(commentId);
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Cannot update another user's comment");
        }
        comment.setText(dto.text());
        return commentMapper.toDto(commentRepository.save(comment));
    }

    @Override
    public CommentDto getCommentByUser(Long userId, Long commentId) {
        getUser(userId);
        Comment comment = getComment(commentId);
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Cannot access another user's comment");
        }
        return commentMapper.toDto(comment);
    }

    @Override
    public List<CommentDto> getUserComments(Long userId,
                                            LocalDateTime createStart,
                                            LocalDateTime createEnd,
                                            int from,
                                            int size) {
        getUser(userId);
        validateRange(createStart, createEnd);
        Pageable pageable = PageRequestUtil.toPageable(from, size);
        List<Comment> comments;
        if (createStart != null && createEnd != null) {
            comments = commentRepository.findAllByAuthorIdAndCreatedBetween(userId, createStart, createEnd, pageable);
        } else if (createStart != null) {
            comments = commentRepository.findAllByAuthorIdAndCreatedGreaterThanEqual(userId, createStart, pageable);
        } else if (createEnd != null) {
            comments = commentRepository.findAllByAuthorIdAndCreatedLessThanEqual(userId, createEnd, pageable);
        } else {
            comments = commentRepository.findAllByAuthorId(userId, pageable);
        }
        return comments.stream()
                .map(commentMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteCommentByUser(Long userId, Long commentId) {
        getUser(userId);
        Comment comment = getComment(commentId);
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Cannot delete another user's comment");
        }
        commentRepository.delete(comment);
    }

    @Override
    public List<CommentDto> getCommentsByEventId(Long eventId, int from, int size) {
        getEvent(eventId);
        return commentRepository
                .findAllByEventId(eventId, PageRequestUtil.toPageable(from, size))
                .stream()
                .map(commentMapper::toDto)
                .toList();
    }

    @Override
    public CommentDto getCommentByAdmin(Long commentId) {
        return commentMapper.toDto(getComment(commentId));
    }

    @Override
    @Transactional
    public CommentDto updateCommentByAdmin(Long commentId, NewCommentDto dto) {
        Comment comment = getComment(commentId);
        comment.setText(dto.text());
        return commentMapper.toDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteCommentByAdmin(Long commentId) {
        commentRepository.delete(getComment(commentId));
    }

    private void validateRange(LocalDateTime createStart, LocalDateTime createEnd) {
        if (createStart != null && createEnd != null && createStart.isAfter(createEnd)) {
            throw new BadRequestException("createStart must be before createEnd");
        }
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }

    private Comment getComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " was not found"));
    }
}
