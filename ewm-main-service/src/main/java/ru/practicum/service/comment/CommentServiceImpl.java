package ru.practicum.service.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.CommentMapper;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ForbiddenException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.model.Comment;
import ru.practicum.model.Event;
import ru.practicum.model.EventState;
import ru.practicum.model.User;
import ru.practicum.repository.CommentRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.validator.StaticValidator;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CommentMapper commentMapper;

    @Override
    public CommentDto addNew(long userId, long eventId, NewCommentDto newCommentDto) {
        User user = getUserById(userId);
        Event event = getEventById(eventId);
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Comments can be added only to published events!");
        }
        Comment comment = commentRepository.save(commentMapper.toComment(newCommentDto, event, user));
        return commentMapper.toCommentDto(comment);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CommentDto> getEventComments(long eventId, int from, int size) {
        StaticValidator.validateFromSize(from, size);
        getEventById(eventId);
        List<Comment> comments = commentRepository.findAllByEventId(eventId, PageRequest.of(from / size, size));
        return commentMapper.toCommentDtoList(comments);
    }

    @Override
    public CommentDto updateOwn(long userId, long commentId, NewCommentDto updateDto) {
        Comment comment = getCommentById(commentId);
        if (comment.getAuthor().getId() != userId) {
            throw new ForbiddenException("Only comment owner able to update it!");
        }
        comment.setText(updateDto.getText());
        comment.setUpdatedOn(LocalDateTime.now());
        commentRepository.save(comment);
        return commentMapper.toCommentDto(comment);
    }

    @Override
    public CommentDto updateAdmin(long commentId, NewCommentDto updateDto) {
        Comment comment = getCommentById(commentId);
        comment.setText(updateDto.getText());
        comment.setUpdatedOn(LocalDateTime.now());
        commentRepository.save(comment);
        return commentMapper.toCommentDto(comment);
    }

    @Override
    public void deleteOwn(long userId, long commentId) {
        Comment comment = getCommentById(commentId);
        if (comment.getAuthor().getId() != userId) {
            throw new ForbiddenException("Only comment author able to delete it");
        }
        commentRepository.deleteById(commentId);
    }

    @Override
    public void deleteAdmin(long commentId) {
        getCommentById(commentId);
        commentRepository.deleteById(commentId);
    }

    private Comment getCommentById(long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() ->
                new NotFoundException(String.format("Comment ID = %d not found!", commentId)));
    }

    private User getUserById(long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException(String.format("User ID = %d not found!", userId)));
    }

    private Event getEventById(long id) {
        return eventRepository.findById(id).orElseThrow(() ->
                new NotFoundException(String.format("Event ID = %d not found!", id)));
    }
}
