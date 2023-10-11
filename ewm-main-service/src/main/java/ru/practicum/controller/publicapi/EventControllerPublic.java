package ru.practicum.controller.publicapi;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.EventSort;
import ru.practicum.service.event.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Validated
public class EventControllerPublic {
    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getAllFiltered(@RequestParam(required = false) String text,
                                              @RequestParam(name = "categories", required = false) List<Long> catIds,
                                              @RequestParam(required = false) Boolean paid,
                                              @RequestParam(required = false) LocalDateTime rangeStart,
                                              @RequestParam(required = false) LocalDateTime rangeEnd,
                                              @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                              @RequestParam(required = false) EventSort sort,
                                              @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                              @RequestParam(defaultValue = "10") @Positive int size,
                                              HttpServletRequest request) {
        return eventService.getAllPublicFiltered(
                text, catIds, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size, request);
    }

    @GetMapping("/{id}")
    public EventFullDto getPublicById(@PathVariable @Positive long id, HttpServletRequest request) {
        return eventService.getPublicById(id, request);
    }
}
