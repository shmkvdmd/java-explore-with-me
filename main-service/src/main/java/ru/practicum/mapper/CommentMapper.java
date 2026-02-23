package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.model.Comment;

@Component
public class CommentMapper {
    public CommentDto toDto(Comment comment) {
        if (comment == null) {
            return null;
        }
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .created(comment.getCreated())
                .authorName(comment.getAuthor() == null ? null : comment.getAuthor().getName())
                .eventId(comment.getEvent() == null ? null : comment.getEvent().getId())
                .build();
    }
}
