package ru.practicum.controller.privateapi;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.service.comment.CommentService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

@RestController
@RequestMapping("/users/{userId}/comments")
@RequiredArgsConstructor
@Validated
public class CommentControllerPrivate {
    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addNew(@PathVariable @Positive long userId,
                             @RequestParam @Positive long eventId,
                             @RequestBody @Valid NewCommentDto newCommentDto) {
        return commentService.addNew(userId, eventId, newCommentDto);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateOwn(@PathVariable @Positive long userId,
                                @PathVariable @Positive long commentId,
                                @RequestBody @Valid NewCommentDto updateDto) {
        return commentService.updateOwn(userId, commentId, updateDto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelOwn(@PathVariable @Positive long userId,
                          @PathVariable @Positive long commentId) {
        commentService.deleteOwn(userId, commentId);
    }
}
