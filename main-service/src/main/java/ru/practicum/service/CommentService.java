package ru.practicum.service;

import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentService {
    CommentDto createComment(Long userId, Long eventId, NewCommentDto dto);

    CommentDto updateCommentByUser(Long userId, Long commentId, NewCommentDto dto);

    CommentDto getCommentByUser(Long userId, Long commentId);

    List<CommentDto> getUserComments(Long userId,
                                     LocalDateTime createStart,
                                     LocalDateTime createEnd,
                                     int from,
                                     int size);

    void deleteCommentByUser(Long userId, Long commentId);

    List<CommentDto> getCommentsByEventId(Long eventId, int from, int size);

    CommentDto getCommentByAdmin(Long commentId);

    CommentDto updateCommentByAdmin(Long commentId, NewCommentDto dto);

    void deleteCommentByAdmin(Long commentId);
}
