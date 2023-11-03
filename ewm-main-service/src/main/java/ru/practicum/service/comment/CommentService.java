package ru.practicum.service.comment;

import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;

import java.util.List;

public interface CommentService {
    CommentDto addNew(long userId, long eventId, NewCommentDto newCommentDto);

    List<CommentDto> getEventComments(long eventId, int from, int size);

    CommentDto updateOwn(long userId, long commentId, NewCommentDto updateDto);

    CommentDto updateAdmin(long commentId, NewCommentDto updateDto);

    void deleteOwn(long userId, long commentId);

    void deleteAdmin(long commentId);
}
